package io.ebeaninternal.server.deploy;

import io.ebean.config.DatabaseConfig;
import io.ebean.config.EncryptKey;
import io.ebean.config.NamingConvention;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;
import io.ebeaninternal.server.deploy.parse.TenantDeployCreateProperties;
import io.ebeaninternal.server.deploy.parse.XReadAnnotations;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeanservice.docstore.api.DocStoreBeanAdapter;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BeanDescriptorMapTenant implements BeanDescriptorMap {
  protected static final Logger log = CoreLog.internal;
  private final Lock lock = new ReentrantLock();
  private final Object tenantId;
  private final BeanDescriptorManagerTenant beanDescriptorManager;
  protected final List<BeanDescriptor<?>> elementDescriptors = new LinkedList<>();
  protected final Map<String, BeanTable> beanTableMap = new HashMap<>();
  protected final Map<String, BeanDescriptor<?>> descMap = new HashMap<>();
  protected final Map<String, BeanDescriptor<?>> descQueueMap = new HashMap<>();
  protected final Map<String, BeanManager<?>> beanManagerMap = new HashMap<>();
  protected final Map<Class<?>, DeployBeanInfo<?>> descInfoMap = new HashMap<>();
  protected final Map<String, List<BeanDescriptor<?>>> tableToDescMap = new HashMap<>();
  protected final Map<String, List<BeanDescriptor<?>>> tableToViewDescMap = new HashMap<>();

  protected final XReadAnnotations readAnnotations;
  protected final TenantDeployCreateProperties createProperties;
  protected final Map<Class<?>, DeployBeanInfo<?>> rootInfoMap;

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
  public DatabaseConfig config() {
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
    throw new UnsupportedOperationException();
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
    if (!isDeployed(entityClass)) { //it's a lazy deploy
      return false;
    }
    lock.lock();
    try {
      if (isChanged(entityClass, entity)) {
        deployLocked(entityClass, entity);
      }
    } finally {
      lock.unlock();
    }
    return true;
  }

  protected void deployLocked(Class<?> entityClass) {
    deployLocked(entityClass, null);
  }

  protected void deployLocked(Class<?> entityClass, XEntity entity) {
    BeanDescriptorMapContext context = new BeanDescriptorMapContext(beanTableMap, descMap, descInfoMap, rootInfoMap);
    BeanDescriptorMapTemporal factory = new BeanDescriptorMapTemporal(beanDescriptorManager, context, readAnnotations, createProperties);
    factory.deploy(entityClass, entity);
    factory.initialise();
    registerBeanDescriptor(factory);
  }

  private void registerBeanDescriptor(BeanDescriptorMapTemporal factory) {
    setEbeanServer(factory.descMap, factory);
    descMap.putAll(factory.descMap);
    beanManagerMap.putAll(factory.beanManagerMap);
    descInfoMap.putAll(factory.descInfoMap);
    beanTableMap.putAll(factory.beanTableMap);
    tableToDescMap.putAll(factory.tableToDescMap);
    tableToViewDescMap.putAll(factory.tableToViewDescMap);
  }

  protected void setEbeanServer(Map<String, BeanDescriptor<?>> descMap, BeanDescriptorMapTemporal factory) {
    //10.ebeanServer
    for (BeanDescriptor<?> desc : descMap.values()) {
      if (!factory.beanManagerMap.containsKey(desc.fullName())) {
        beanManagerMap.put(desc.fullName(), beanDescriptorManager.beanManagerFactory.create(desc));
        desc.setEbeanServer(beanDescriptorManager.ebeanServer);
      }
    }
  }

  public BeanDescriptor<?> createBeanDescriptor(Class<?> beanClass, XEntity entity) throws Exception {
    lock.lock();
    try {
      BeanDescriptorMapContext context = new BeanDescriptorMapContext(beanTableMap, descMap, descInfoMap, rootInfoMap);
      BeanDescriptorMapTemporal factory = new BeanDescriptorMapTemporal(beanDescriptorManager, context, readAnnotations, createProperties);
      DeployBeanInfo<?> info = rootInfoMap.get(beanClass);
      DeployBeanInfo<?> newInfo = createProperties.createDeployBeanInfo(entity, beanClass, info, readAnnotations, factory);
      BeanDescriptor<?> desc = factory.deployInfo(beanClass, newInfo);
      factory.initialise();
      return desc;
    } finally {
      lock.unlock();
    }
  }

  protected boolean isChanged(Class<?> beanClass, XEntity entity) {
    String etag = entity.generateEtag();
    String oldEtag = getDesc(beanClass).dbComment();
    return !etag.equals(oldEtag);
  }
}
