package io.ebeaninternal.server.loadcontext;

import io.ebean.CacheMode;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollectionLoader;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.PersistenceContext;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.api.LoadManyBuffer;
import io.ebeaninternal.api.LoadSecondaryQuery;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.querydefn.OrmQueryProperties;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ToMany bean load context.
 */
final class DLoadComputedContext implements LoadSecondaryQuery {
  protected final ReentrantLock lock = new ReentrantLock();
  protected final DLoadContext parent;
  protected final BeanProperty property;
  protected final String fullPath;
  protected final String serverName;
  private List<LoadBuffer> bufferList;
  private LoadBuffer currentBuffer;
  final int batchSize;
  final boolean queryFetch;
  final ObjectGraphNode objectGraphNode;
  final OrmQueryProperties queryProps;

  DLoadComputedContext(DLoadContext parent, BeanProperty property, String path, OrmQueryProperties queryProps) {
    this.parent = parent;
    this.property = property;
    this.serverName = parent.server().name();
    this.fullPath = parent.fullPath(path);
    this.queryProps = queryProps;
    this.queryFetch = queryProps != null && queryProps.isQueryFetch();
    this.batchSize = parent.batchSize(queryProps);
    this.objectGraphNode = parent.objectGraphNode(path);
    // bufferList only required when using query joins (queryFetch)
    this.bufferList = (!queryFetch) ? null : new ArrayList<>();
    this.currentBuffer = createBuffer(batchSize);
  }

  private LoadBuffer createBuffer(int size) {
    LoadBuffer buffer = parent.useReferences ? new LoadBufferWeakRef(this, size) : new LoadBufferHardRef(this, size);
    if (bufferList != null) {
      bufferList.add(buffer);
    }
    return buffer;
  }

  /**
   * Reset the buffers for a query iterator reset.
   */
  private void clear() {
    if (bufferList != null) {
      bufferList.clear();
    }
    currentBuffer = createBuffer(batchSize);
  }

  void setLabel(SpiQuery<?> query) {
    String label = parent.planLabel();
    if (label != null) {
      query.setProfilePath(label, fullPath, parent.profileLocation());
    }
  }

  private void configureQuery(SpiQuery<?> query) {
    setLabel(query);
    parent.propagateQueryState(query, false);
    query.setParentNode(objectGraphNode);
    if (queryProps != null) {
      queryProps.configureBeanQuery(query);
    }
  }

  public String getName() {
    return parent.server().name();
  }

  public void register(BeanCollection<?> bc) {
    if (currentBuffer.isFull()) {
      currentBuffer = createBuffer(batchSize);
    }
    currentBuffer.add(bc);
    bc.setLoader(currentBuffer);
  }

  @Override
  public void loadSecondaryQuery(OrmQueryRequest<?> parentRequest, boolean forEach) {
    if (!queryFetch) {
      throw new IllegalStateException("Not expecting loadSecondaryQuery() to be called?");
    }
    lock.lock();
    try {
      if (bufferList != null) {
        for (LoadBuffer loadBuffer : bufferList) {
          if (loadBuffer.size() > 0) {
            loadBuffer.loadValueInternal(parent.server());
          }
        }
        if (forEach) {
          clear();
        } else {
          // this is only run once - secondary query is a one shot deal
          this.bufferList = null;
        }
      }
    } finally {
      lock.unlock();
    }
  }

  PersistenceContext getPersistenceContext() {
    return parent.persistenceContext();
  }

  /**
   * A buffer for batch loading bean collections on a given path.
   * Supports batch lazy loading and secondary query loading.
   */
  static abstract class LoadBuffer implements BeanCollectionLoader, LoadManyBuffer {

    private final ReentrantLock lock = new ReentrantLock();
    private final PersistenceContext persistenceContext;
    private final DLoadComputedContext context;
    final int batchSize;

    LoadBuffer(DLoadComputedContext context, int batchSize) {
      this.context = context;
      // set the persistence context as at this moment in
      // case it changes as part of a findIterate etc
      this.persistenceContext = context.getPersistenceContext();
      this.batchSize = batchSize;
    }

    @Override
    public boolean isUseDocStore() {
      return false;
    }

    @Override
    public int batchSize() {
      return batchSize;
    }

    /**
     * Return true if the buffer is full.
     */
    public boolean isFull() {
      return batchSize() == size();
    }

    /**
     * Return true if the buffer is full.
     */
    public abstract void add(BeanCollection<?> bc);

    abstract void clear();

    @Override
    public BeanProperty beanProperty() {
      return context.property;
    }

    @Override
    public ObjectGraphNode objectGraphNode() {
      return context.objectGraphNode;
    }

    @Override
    public void configureQuery(SpiQuery<?> query) {
      context.configureQuery(query);
    }

    @Override
    public String name() {
      return context.serverName;
    }

    @Override
    public BeanDescriptor<?> descriptor() {
      return context.property.descriptor();
    }

    @Override
    public PersistenceContext persistenceContext() {
      return persistenceContext;
    }

    @Override
    public String fullPath() {
      return context.fullPath;
    }

    @Override
    public void loadMany(BeanCollection<?> bc, boolean onlyIds) {
      lock.lock();
      try {
        //context.parent.getEbeanServer().loadMany(new LoadManyRequest(this, onlyIds, false));
        loadValueInternal(context.parent.server());
        // clear the buffer as all entries have been loaded
        clear();
      } finally {
        lock.unlock();
      }
    }

    private Map<Object, BeanCollection<?>> parentIdList(SpiEbeanServer server, BeanDescriptor<?> desc) {
      Map<Object, BeanCollection<?>> idList = new HashMap<>(size() * 2);
      for (int i = 0; i < size(); i++) {
        BeanCollection<?> bc = get(i);
        if (bc != null) {
          idList.put(desc.id(bc.owner()), bc);
          bc.addBean(null);
          bc.setLoader(server); // don't use the load buffer again
        }
      }
      return idList;
    }

    private void loadValueInternal(SpiEbeanServer server) {
      BeanProperty property = context.property;
      BeanDescriptor<?> desc = property.descriptor();
      BeanProperty idProperty = desc.idProperty();
      ScalarType<?> scalarId = idProperty.scalarType();
      ScalarType<?> scalarType = property.scalarType();
      Map<Object, BeanCollection<?>> idList = parentIdList(server, desc);

      SpiQuery<?> query = (SpiQuery<?>) server.createQuery(desc.type());
      query.setLoadDescription("+lazy", null);
      query.select(idProperty.name() + "," + property.name());
      query.where().idIn(idList.keySet());
      query.setBeanCacheMode(CacheMode.OFF);
      query.setMode(SpiQuery.Mode.LAZYLOAD_COMPUTED);
      query.setType(SpiQuery.Type.ID_LIST);
      query.setPersistenceContext(persistenceContext);

      // potentially changes the joins, selected properties, cache mode
      this.configureQuery(query);

      query.asDto(Object[].class).findEach(data -> {
        Object id = scalarId.toBeanType(data[0]);
        BeanCollection bc = idList.get(id);
        if (bc != null) {
          bc.addBean(scalarType.toBeanType(data[1]));
        }
      });
    }
  }

  static class LoadBufferHardRef extends LoadBuffer {
    private final BeanCollection<?>[] list;

    private int size;

    LoadBufferHardRef(DLoadComputedContext context, int batchSize) {
      super(context, batchSize);
      this.list = new BeanCollection<?>[batchSize];
    }

    /**
     * Return true if the buffer is full.
     */
    @Override
    public void add(BeanCollection<?> bc) {
      list[size++] = bc;
    }

    @Override
    void clear() {
      Arrays.fill(list, null);
      size = 0;
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public BeanCollection<?> get(int i) {
      return list[i];
    }

    @Override
    public boolean removeFromBuffer(BeanCollection<?> collection) {
      for (int i = 0; i < size; i++) {
        // find it using instance equality - avoiding equals() and potential deadlock issue
        if (list[i] == collection) {
          list[i] = null;
          return true;
        }
      }
      return false;
    }
  }

  /**
   * This load buffer uses weak references, so unreachable beanCollections will drop out from the buffer.
   */
  static class LoadBufferWeakRef extends LoadBuffer {
    private final Reference<BeanCollection<?>>[] list;

    private int size;

    LoadBufferWeakRef(DLoadComputedContext context, int batchSize) {
      super(context, batchSize);
      this.list = new Reference[batchSize];
    }

    /**
     * Return true if the buffer is full.
     */
    @Override
    public void add(BeanCollection<?> bc) {
      list[size++] = new WeakReference<>(bc);
    }

    @Override
    void clear() {
      Arrays.fill(list, null);
      size = 0;
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public BeanCollection<?> get(int i) {
      Reference<BeanCollection<?>> ref = list[i];
      if (ref == null) {
        return null;
      }
      BeanCollection<?> bc = ref.get();
      if (bc == null) {
        // remove dead references
        list[i] = null;
      }
      return bc;
    }

    @Override
    public boolean removeFromBuffer(BeanCollection<?> collection) {
      for (int i = 0; i < size; i++) {
        if (list[i] != null) {
          BeanCollection<?> bc = list[i].get();
          if (bc == null) {
            // remove dead references
            list[i] = null;
          }
          // find it using instance equality - avoiding equals() and potential deadlock issue
          if (bc == collection) {
            list[i] = null;
            return true;
          }
        }
      }
      return false;
    }
  }
}
