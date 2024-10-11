package io.ebeaninternal.api;

import io.ebean.CacheMode;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.api.SpiQuery.Mode;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Request for loading ManyToOne and OneToOne relationships.
 */
public final class LoadBeanRequest extends LoadRequest {

  private final Set<EntityBeanIntercept> batch;
  private final LoadBeanBuffer loadBuffer;
  private final String lazyLoadProperty;
  private final boolean loadCache;
  private final boolean alreadyLoaded;
  private String triggerEbi;
  private String batchBefore;
  private List<Object> queryIds;

  /**
   * Construct for lazy load request.
   */
  public LoadBeanRequest(LoadBeanBuffer loadBuffer, EntityBeanIntercept ebi, boolean loadCache) {
    this(loadBuffer, null, true, ebi.lazyLoadProperty(), ebi.isLoaded(), loadCache || ebi.isLoadedFromCache());
    this.triggerEbi = String.valueOf(ebi);
  }

  /**
   * Construct for secondary query.
   */
  public LoadBeanRequest(LoadBeanBuffer loadBuffer, OrmQueryRequest<?> parentRequest) {
    this(loadBuffer, parentRequest, false, null, false, false);
  }

  private LoadBeanRequest(LoadBeanBuffer loadBuffer, OrmQueryRequest<?> parentRequest, boolean lazy,
                          String lazyLoadProperty, boolean alreadyLoaded, boolean loadCache) {
    super(parentRequest, lazy);
    this.loadBuffer = loadBuffer;
    this.batch = loadBuffer.batch();
    this.lazyLoadProperty = lazyLoadProperty;
    this.alreadyLoaded = alreadyLoaded;
    this.loadCache = loadCache;
  }

  @Override
  public Class<?> beanType() {
    return loadBuffer.descriptor().type();
  }

  public String description() {
    return loadBuffer.fullPath();
  }

  /**
   * Return true if the batch is empty.
   */
  public boolean checkEmpty() {
    loadBuffer.loadingStarted();
    batchBefore = String.valueOf(batch);
    queryIds = ids();
    return batch.isEmpty();
  }

  /**
   * Return the list of Id values for the beans in the lazy load buffer.
   */
  private List<Object> ids() {
    final List<Object> idList = new ArrayList<>(batch.size());
    final BeanDescriptor<?> desc = loadBuffer.descriptor();
    for (EntityBeanIntercept ebi : batch) {
      idList.add(desc.getId(ebi.owner()));
    }
    return idList;
  }

  public SpiQuery<?> createQuery(SpiEbeanServer server) {
    final SpiQuery<?> query = server.createQuery(beanType());
    query.usingTransaction(transaction);
    configureQuery(query);
    return query;
  }

  /**
   * Configure the query for lazy loading execution.
   */
  private void configureQuery(SpiQuery<?> query) {
    query.setMode(Mode.LAZYLOAD_BEAN);
    query.setPersistenceContext(loadBuffer.persistenceContext());
    query.setLoadDescription(mode(), description());
    if (lazy) {
      query.setLazyLoadBatchSize(loadBuffer.batchSize());
      if (alreadyLoaded) {
        query.setBeanCacheMode(CacheMode.OFF);
      }
    } else {
      query.setBeanCacheMode(CacheMode.OFF);
    }
    loadBuffer.configureQuery(query, lazyLoadProperty);
    if (loadCache) {
      query.setBeanCacheMode(CacheMode.PUT);
    }
    if (queryIds.size() == 1) {
      query.where().idEq(queryIds.get(0));
    } else {
      query.where().idIn(queryIds);
    }
  }

  private String mode() {
    return lazy ? "lazy" : loadBuffer.isCache() ? "cache" : "query";
  }

  /**
   * Load the beans into the L2 cache if that is requested and check for load failures due to deletes.
   */
  public void postLoad(List<?> list) {
    loadBuffer.loadingStopped();
    Set<Object> loadedIds = new HashSet<>();
    BeanDescriptor<?> desc = loadBuffer.descriptor();
    // collect Ids and maybe load bean cache
    for (Object bean : list) {
      loadedIds.add(desc.id(bean));
    }
    if (loadCache) {
      desc.cacheBeanPutAll(list);
    }
    if (lazyLoadProperty != null) {
      List<EntityBeanIntercept> missed = new ArrayList<>();
      Set<Object> missedIds = new HashSet<>();
      for (EntityBeanIntercept ebi : batch) {
        // check if the underlying row in DB was deleted. Mark the bean as 'failed' if
        // necessary but allow processing to continue until it is accessed by client code
        Object id = desc.getId(ebi.owner());
        if (!loadedIds.contains(id)) {
          if (queryIds.contains(id)) {
            // assume this is logically deleted (hence not found)
            desc.markAsDeleted(ebi.owner());
          } else {
            // unexpected, added to batch during loading
            missedIds.add(id);
            missed.add(ebi);
          }
        }
        if (!missed.isEmpty()) {
          if (CoreLog.markedAsDeleted.isLoggable(DEBUG)) {
            CoreLog.markedAsDeleted.log(DEBUG, "Loaded bean batch triggered by ebi:{0} property:{1}", triggerEbi, lazyLoadProperty);
            CoreLog.markedAsDeleted.log(DEBUG, "Loaded bean batch BEFORE {0}", batchBefore);
            CoreLog.markedAsDeleted.log(DEBUG, "Loaded bean batch AFTER {0}", batch);
            String msg = MessageFormat.format("Bean added to batch during load for {0} missedIds:{1} queryIds:{2} missed:{3}",
              beanType(), missedIds, queryIds, missed);
            CoreLog.markedAsDeleted.log(DEBUG, msg, new RuntimeException("LoadBeanRequest - Bean added to batch during load"));
          }
        }
      }
    }
  }
}
