package io.ebeaninternal.server.deploy;

import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.EncryptKey;
import io.ebean.config.NamingConvention;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;
import io.ebeaninternal.server.deploy.parse.TenantDeployCreateProperties;
import io.ebeaninternal.server.deploy.parse.XReadAnnotations;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeanservice.docstore.api.DocStoreBeanAdapter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class BeanDescriptorMapTenant implements BeanDescriptorMap {
  private final Lock lock = new ReentrantLock();
  private final Object tenantId;
  private final BeanDescriptorManagerTenant beanDescriptorManager;
  protected final List<BeanDescriptor<?>> elementDescriptors = new LinkedList<>();
  protected final Map<String, BeanTable> beanTableMap = new ConcurrentHashMap<>();
  protected final Map<String, BeanDescriptor<?>> descMap = new ConcurrentHashMap<>();
  protected final Map<String, BeanDescriptor<?>> descQueueMap = new ConcurrentHashMap<>();
  protected final List<BeanDescriptorConsumer> descListeners = new LinkedList<>();
  protected final Map<String, BeanManager<?>> beanManagerMap = new ConcurrentHashMap<>();
  protected final Map<Class<?>, DeployBeanInfo<?>> descInfoMap = new ConcurrentHashMap<>();
  protected final Map<String, List<BeanDescriptor<?>>> tableToDescMap = new ConcurrentHashMap<>();
  protected final Map<String, List<BeanDescriptor<?>>> tableToViewDescMap = new ConcurrentHashMap<>();

  protected final XReadAnnotations readAnnotations;
  protected final TenantDeployCreateProperties createProperties;
  protected final Map<Class<?>, DeployBeanInfo<?>> rootInfoMap;

  private final ThreadLocal<BeanDescriptorMapTemporal> current = new ThreadLocal<>();

  public BeanDescriptorMapTenant(Object tenantId, BeanDescriptorManagerTenant beanDescriptorManager) {
    this.tenantId = tenantId;
    this.beanDescriptorManager = beanDescriptorManager;
    this.readAnnotations = beanDescriptorManager.readAnnotations;
    this.createProperties = beanDescriptorManager.tenantCreateProperties;
    this.rootInfoMap = Collections.unmodifiableMap(beanDescriptorManager.deployInfoMap);
  }

  @Override
  public String name() {
    return beanDescriptorManager.name() + "." + tenantId;
  }

  @Override
  public DatabaseBuilder.Settings config() {
    return beanDescriptorManager.config();
  }

  @Override
  public SpiCacheManager cacheManager() {
    return beanDescriptorManager.cacheManager();
  }

  @Override
  public NamingConvention namingConvention() {
    return beanDescriptorManager.namingConvention();
  }

  @Override
  public boolean isMultiValueSupported() {
    return beanDescriptorManager.isMultiValueSupported();
  }

  @Override
  public EncryptKey encryptKey(String tableName, String columnName) {
    return beanDescriptorManager.encryptKey(tableName, columnName);
  }

  @Override
  public IdBinder createIdBinder(BeanProperty id) {
    return beanDescriptorManager.createIdBinder(id);
  }

  @Override
  public <T> DocStoreBeanAdapter<T> createDocStoreBeanAdapter(BeanDescriptor<T> descriptor, DeployBeanDescriptor<T> deploy) {
    return beanDescriptorManager.createDocStoreBeanAdapter(descriptor, deploy);
  }

  @Override
  public ScalarType<?> scalarType(int jdbcType) {
    return beanDescriptorManager.scalarType(jdbcType);
  }

  @Override
  public ScalarType<?> scalarType(String cast) {
    return beanDescriptorManager.scalarType(cast);
  }

  @Override
  public boolean isJacksonCorePresent() {
    return beanDescriptorManager.isJacksonCorePresent();
  }

  @Override
  public boolean isTableManaged(String tableName) {
    return isLocalTableManaged(tableName) || beanDescriptorManager.isTableManaged(tableName);
  }

  private boolean isLocalTableManaged(String tableName) {
    return tableToDescMap.get(tableName.toLowerCase()) != null || tableToViewDescMap.get(tableName.toLowerCase()) != null;
  }

  @Override
  public BeanTable beanTable(Class<?> beanClass) {
    BeanDescriptorMapTemporal that = current.get();
    if (that != null) {
      return that.beanTable(beanClass);
    }
    return beanTableMap.get(beanClass.getName());
  }

  @Override
  public <T> BeanDescriptor<T> descriptor(Class<T> beanClass) {
    if (!isDeployed(beanClass)) {
      deploy(beanClass);
    }
    BeanDescriptor<T> desc = getDesc(beanClass);
    if (desc == null) {
      throw new IllegalStateException(String.format("%s bean descriptor is null", beanClass.getName()));
    }
    return desc;
  }

  public <T> BeanDescriptor<T> getDesc(Class<T> beanClass) {
    BeanDescriptorMapTemporal that = current.get();
    if (that != null) {
      return that.desc(beanClass);
    }
    return (BeanDescriptor<T>) descMap.get(beanClass.getName());
  }

  public <T> BeanManager<T> getBeanManager(Class<T> beanClass) {
    return (BeanManager<T>) beanManagerMap.get(beanClass.getName());
  }

  public <T> BeanManager<T> beanManager(Class<T> beanClass) {
    if (!isDeployed(beanClass)) {
      deploy(beanClass);
    }
    BeanManager<T> desc = getBeanManager(beanClass);
    if (desc == null) {
      throw new IllegalStateException(String.format("%s bean manager is null", beanClass.getName()));
    }
    return desc;
  }

  protected boolean isDeployed(Class<?> entityClass) {
    return descMap.containsKey(entityClass.getName());
  }

  public void deploy(Class<?> entityClass) {
    if (isDeployed(entityClass)) {
      return;
    }
    lock.lock();
    try {
      if (!isDeployed(entityClass)) {
        deployLocked(entityClass);
      }
    } finally {
      lock.unlock();
    }
  }

  public boolean redeploy(Class<?> entityClass, XEntity entity) {
    lock.lock();
    try {
      if (isChanged(entityClass, entity)) {
        redeployLocked(entityClass, entity);
      }
    } finally {
      lock.unlock();
    }
    return true;
  }

  protected void deployLocked(Class<?> entityClass) {
    deployLocked(entityClass, null);
  }

  protected void redeployLocked(Class<?> entityClass, XEntity entity) {
    BeanDescriptorMapContext context = new BeanDescriptorMapRedeployContext(entityClass, beanTableMap, descMap, descInfoMap, rootInfoMap);
    deployLocked(context, entityClass, entity);
  }

  protected void deployLocked(Class<?> entityClass, XEntity entity) {
    BeanDescriptorMapContext context = new BeanDescriptorMapContext(beanTableMap, descMap, descInfoMap, rootInfoMap);
    deployLocked(context, entityClass, entity);
  }

  protected void deployLocked(BeanDescriptorMapContext context, Class<?> entityClass, XEntity entity) {
    BeanDescriptorMapTemporal factory = current.get();
    if (factory == null) {
      factory = new BeanDescriptorMapTemporal(tenantId, beanDescriptorManager, context, readAnnotations, createProperties);
      current.set(factory);
      try {
        factory.deploy(entityClass, entity);
        factory.initialise();
        registerBeanDescriptor(factory);
      } finally {
        factory.clear();
        current.remove();
      }
    } else {
      factory.deploy(entityClass, entity);
    }
  }

  private void registerBeanDescriptor(BeanDescriptorMapTemporal factory) {
    setEbeanServer(factory.descMap, factory);
    elementDescriptors.addAll(factory.elementDescriptors);
    descMap.putAll(factory.descMap);
    descQueueMap.putAll(factory.descQueueMap);
    beanManagerMap.putAll(factory.beanManagerMap);
    descInfoMap.putAll(factory.descInfoMap);
    beanTableMap.putAll(factory.beanTableMap);
    tableToDescMap.putAll(factory.tableToDescMap);
    tableToViewDescMap.putAll(factory.tableToViewDescMap);
    notifyBeanDescriptorConsumer(factory);
  }

  private void notifyBeanDescriptorConsumer(BeanDescriptorMapTemporal factory) {
    List<BeanDescriptorConsumer> removed = new LinkedList<>();
    for (BeanDescriptor<?> desc : factory.descMap.values()) {
      for (BeanDescriptorConsumer l : descListeners) {
        if (l.isOwner(desc.type())) {
          removed.add(l);
        } else {
          l.callback(desc);
        }
      }
    }
    descListeners.removeAll(removed);
    descListeners.addAll(factory.descListeners);
  }

  protected void setEbeanServer(Map<String, BeanDescriptor<?>> descMap, BeanDescriptorMapTemporal factory) {
    //10.ebeanServer
    for (BeanDescriptor<?> desc : descMap.values()) {
      if (!desc.isEmbedded() && !factory.beanManagerMap.containsKey(desc.fullName())) {
        beanManagerMap.put(desc.fullName(), beanDescriptorManager.beanManagerFactory.create(desc));
        desc.setEbeanServer(beanDescriptorManager.ebeanServer);
      }
    }
  }

  public BeanDescriptor<?> createBeanDescriptor(Class<?> beanClass, XEntity entity) {
    lock.lock();
    try {
      BeanDescriptorMapContext context = new BeanDescriptorMapContext(beanTableMap, descMap, descInfoMap, rootInfoMap);
      BeanDescriptorMapTemporal factory = new BeanDescriptorMapTemporal(tenantId, beanDescriptorManager, context, readAnnotations, createProperties);
      BeanDescriptorMapTemporal.DeployInfo newInfo = factory.createDeployBeanInfo(beanClass, entity);
      BeanDescriptor<?> desc = factory.deployInfo(beanClass, newInfo);
      factory.initialise();
      factory.clear();
      return desc;
    } finally {
      lock.unlock();
    }
  }

  protected boolean isChanged(Class<?> beanClass, XEntity entity) {
    //String etag = entity.generateEtag();
    //String oldEtag = getDesc(beanClass).dbComment();
    return true;
  }

  @Override
  public <T> void listenDescriptor(Class<?> entityType, Class<T> targetClass, Consumer<BeanDescriptor<T>> consumer) {
    throw new UnsupportedOperationException();
  }
}
