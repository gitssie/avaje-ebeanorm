package io.ebeaninternal.server.deploy;

import io.ebean.Model;
import io.ebean.bean.ObjectEntity;
import io.ebean.config.BeanNotEnhancedException;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.EncryptKey;
import io.ebean.config.NamingConvention;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.deploy.meta.*;
import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;
import io.ebeaninternal.server.deploy.parse.TenantDeployCreateProperties;
import io.ebeaninternal.server.deploy.parse.XReadAnnotations;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeaninternal.server.properties.BeanPropertyGetter;
import io.ebeaninternal.server.properties.BeanPropertySetter;
import io.ebeaninternal.server.properties.BiConsumerPropertyAccess;
import io.ebeanservice.docstore.api.DocStoreBeanAdapter;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BeanDescriptorMapTenant implements BeanDescriptorMap {
  protected static final Logger log = CoreLog.internal;
  private final Lock lock = new ReentrantLock();
  private final ThreadLocal<List<BeanDescriptor<?>>> initializeDAG = new ThreadLocal<>();
  private final ThreadLocal<BeanDescriptorMapTemporal> deployInstance = new ThreadLocal<>();
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
    return tableToDescMap.get(tableName.toLowerCase()) != null
      || tableToViewDescMap.get(tableName.toLowerCase()) != null;
  }

  @Override
  public BeanTable beanTable(Class<?> type) {
    BeanTable table = beanTableMap.get(type.getName());
    if (table == null) {
      table = createBeanTable(type);
      if (table == null) {
        throw new IllegalStateException(String.format("%s bean table is null", type.getName()));
      }
    }
    return table;
  }

  @Override
  public <T> BeanDescriptor<T> descriptor(Class<T> beanClass) {
    if (!isDeployed(beanClass)) {
      lock.lock();
      try {
        deploy(beanClass);
      } finally {
        lock.unlock();
      }
    }
    BeanDescriptor<T> desc = (BeanDescriptor<T>) descMap.get(beanClass.getName());
    if (desc == null) {
      throw new IllegalStateException(String.format("%s bean descriptor is null", beanClass.getName()));
    }
    return desc;
  }

  public <T> BeanManager<T> beanManager(Class<T> entityType) {
    if (entityType == Object.class) {
      return null;
    }
    BeanManager<T> desc = (BeanManager<T>) beanManagerMap.get(entityType.getName());
    if (desc == null) {
      deploy(entityType);
      desc = (BeanManager<T>) beanManagerMap.get(entityType.getName());
      if (desc == null) {
        throw new IllegalStateException(String.format("%s bean manager is null", entityType.getName()));
      }
    }
    return desc;
  }

  public DeployBeanInfo<?> descInfo(Class<?> beanClass) {
    BeanDescriptorMapTemporal descriptorMapTemporal = deployInstance.get();
    if (descriptorMapTemporal != null) {
      return descriptorMapTemporal.descInfo(beanClass);
    }
    DeployBeanInfo<?> info = descInfoMap.get(beanClass);
    if (info == null) {
      info = rootInfoMap.get(beanClass);
    }
    return info;
  }

  protected boolean isDeploying(Class<?> entityClass) {
    return beanTableMap.containsKey(entityClass.getName());
  }

  protected boolean isDeployed(Class<?> entityClass) {
    return descMap.containsKey(entityClass.getName());
  }

  public void deploy(Class<?> entityClass) {
    if (isDeploying(entityClass)) {
      return;
    }
    lock.lock();
    try {
      if (!isDeploying(entityClass)) {
        deployLocked(entityClass);
      }
    } finally {
      lock.unlock();
    }
  }

  public boolean redeploy(Class<?> entityClass, XEntity entity) {
    if (!isDeploying(entityClass)) { //it's a lazy deploy
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

  protected boolean isChanged(Class<?> entityClass, XEntity entity) {
    String etag = entity.generateEtag();
    String oldEtag = descMap.get(entityClass.getName()).dbComment();
    return !etag.equals(oldEtag);
  }

  protected void deployLocked(Class<?> entityClass) {
    deployLocked(entityClass, null);
  }

  protected void deployLocked(Class<?> entityClass, XEntity entity) {
    boolean isInit = false;
    try {
      List<BeanDescriptor<?>> descriptors = initializeDAG.get();
      if (descriptors == null) {
        descriptors = new LinkedList<>();
        initializeDAG.set(descriptors);
        isInit = true;
      }
      deployInternal(entityClass, entity, descriptors);
      if (isInit) {
        initialise(descriptors);
      }
    } catch (BeanNotEnhancedException e) {
      undeploy(entityClass);
      throw e;
    } catch (RuntimeException e) {
      undeploy(entityClass);
      log.error("Error in deployment", e);
      throw e;
    } catch (Throwable e) {
      undeploy(entityClass);
      throw e;
    } finally {
      if (isInit) {
        initializeDAG.set(null);
      }
    }
  }

  protected void deployInternal(Class<?> entityClass, XEntity entity, List<BeanDescriptor<?>> descriptors) {
    //1.createListeners();
    //2.readEntityDeploymentInitial
    DeployBeanInfo<?> info = createDeployBeanInfo(entityClass, entity);
    if (info == null) {
      return;
    }
    deploy(info);

    BeanDescriptor<?> desc = registerDescriptor(info);
    descriptors.add(desc);
  }

  protected BeanTable createBeanTable(Class<?> entityClass) {
    DeployBeanInfo<?> info = createDeployBeanInfo(entityClass);
    if (info == null) {
      return null;
    }
    BeanTable beanTable = createBeanTable(info);
    return beanTable;
  }

  protected void deploy(DeployBeanInfo<?> info) {
    //3.readEntityBeanTable();
    readEntityBeanTable(info);
    //4.readEntityDeploymentAssociations();
    readDeployAssociations(info);
    //5.readInheritedIdGenerators 需要父类也进行注册才行
    readInheritedIdGenerators(info);
    //6.7.8.9.creates the BeanDescriptors
    readEntityRelationships(info);
  }

  protected DeployBeanInfo<?> createDeployBeanInfo(final Class<?> beanClass) {
    return createDeployBeanInfo(beanClass, null);
  }

  protected DeployBeanInfo<?> createDeployBeanInfo(final Class<?> beanClass, final XEntity entity) {
    if (entity == null && descInfoMap.get(beanClass) != null) {
      return descInfoMap.get(beanClass);
    }
    DeployBeanInfo<?> info = rootInfoMap.get(beanClass); //这里是从父级集成来的
    Class<?> clazz = beanClass;
    while (info == null && !(clazz.equals(Object.class) || clazz.equals(Model.class))) {
      clazz = clazz.getSuperclass();
      info = rootInfoMap.get(clazz);
    }
    if (info == null) {
      throw new IllegalStateException(
        "If you are running in an IDE with enhancement plugin try a Build -> Rebuild Project to recompile and enhance all entity beans. " +
          "Error - for type " + beanClass);
    }
    try {
      DeployBeanInfo<?> newInfo;
      if (entity != null) {
        newInfo = createProperties.createDeployBeanInfo(entity, beanClass, info, readAnnotations, this);
      } else {
        newInfo = createProperties.createDeployBeanInfo(beanClass, info, readAnnotations, this);
      }
      if (newInfo == info) { //Class的属性没有发生变化,部署的是同一个
        BeanManager beanManager = beanDescriptorManager.beanManager(beanClass.getName());
        if (beanManager != null) {
          registerBeanManager(beanManager);
          return null;
        }
      }
      descInfoMap.put(beanClass, newInfo);
      return newInfo;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  protected BeanTable readEntityBeanTable(DeployBeanInfo<?> info) {
    BeanTable beanTable = beanTableMap.get(info.getDescriptor().getBeanType());
    if (beanTable != null) {
      return beanTable;
    }
    beanTable = createBeanTable(info);
    beanTableMap.put(beanTable.getBeanType().getName(), beanTable);
    return beanTable;
  }

  protected BeanTable createBeanTable(DeployBeanInfo<?> info) {
    DeployBeanDescriptor<?> deployDescriptor = info.getDescriptor();
    DeployBeanTable beanTable = deployDescriptor.createDeployBeanTable();
    return new BeanTable(beanTable, this);
  }

  protected <T> void readDeployAssociations(DeployBeanInfo<T> info, DeployBeanDescriptor<T> desc) {
    for (DeployBeanProperty prop : desc.propertiesAssocOne()) {
      DeployBeanPropertyAssoc assoc = (DeployBeanPropertyAssoc) prop;
      if (assoc.isId() || assoc.isTransient() || assoc.isEmbedded()) {
        continue;
      }
      deploy(assoc.getTargetType());
    }
    for (DeployBeanProperty prop : desc.propertiesAssocMany()) {
      DeployBeanPropertyAssoc assoc = (DeployBeanPropertyAssoc) prop;
      if (assoc.isId() || assoc.isTransient() || assoc.isEmbedded()) {
        continue;
      }
      deploy(assoc.getTargetType());
    }
  }

  protected <T> void readDeployAssociations(DeployBeanInfo<T> info) {
    DeployBeanDescriptor<T> desc = info.getDescriptor();

    readAnnotations.readAssociations(info, this);

    readDeployAssociations(info, desc);

    if (BeanDescriptor.EntityType.SQL == desc.getEntityType()) {
      desc.setBaseTable(null, null, null);
    }
    // mark transient properties
    beanDescriptorManager.transientProperties.process(desc);
    beanDescriptorManager.setScalarType(desc);
    if (!desc.isEmbedded()) {
      // Set IdGenerator or use DB Identity
      beanDescriptorManager.setIdGeneration(desc);
      // find the appropriate default concurrency mode
      beanDescriptorManager.setConcurrencyMode(desc);
    }
    // generate the byte code
    setBeanReflect(desc);
  }

  protected void registerBeanManager(BeanManager beanManager) {
    Class<?> beanClass = beanManager.getBeanDescriptor().beanType;
    descMap.put(beanClass.getName(), beanManager.getBeanDescriptor());
    beanManagerMap.put(beanClass.getName(), beanManager);
  }

  protected BeanDescriptor<?> registerDescriptor(DeployBeanInfo<?> info) {
    BeanDescriptor<?> desc = new BeanDescriptor<>(this, info.getDescriptor());
    descMap.put(desc.type().getName(), desc);
    if (desc.isDocStoreMapped()) {
      descQueueMap.put(desc.docStoreQueueId(), desc);
    }
    for (BeanPropertyAssocMany<?> many : desc.propertiesMany()) {
      if (many.isElementCollection()) {
        elementDescriptors.add(many.elementDescriptor());
      }
    }
    return desc;
  }

  protected void undeploy(Class<?> beanClass) {
    String key = beanClass.getName();
    beanTableMap.remove(key);
    descMap.remove(key);
    beanManagerMap.remove(key);
    descInfoMap.remove(beanClass);
  }

  protected void initialise(List<BeanDescriptor<?>> descriptors) {
    //7.initialiseAll
    initialiseAll(descriptors);

    for (BeanDescriptor<?> desc : descriptors) {
      //8.readForeignKeys
      readForeignKeys(desc);
      //9.readTableToDescriptor();
      readTableToDescriptor(desc);

      setEbeanServer(desc);
    }
  }

  protected void setEbeanServer(BeanDescriptor<?> desc) {
    //10.ebeanServer
    // putting mapping bean
    beanManagerMap.put(desc.fullName(), beanDescriptorManager.beanManagerFactory.create(desc));
    desc.setEbeanServer(beanDescriptorManager.ebeanServer);
  }

  protected void readForeignKeys(BeanDescriptor<?> desc) {
    desc.initialiseFkeys();
  }

  protected void readInheritedIdGenerators(DeployBeanInfo<?> info) {
    DeployBeanDescriptor<?> descriptor = info.getDescriptor();
    InheritInfo inheritInfo = descriptor.getInheritInfo();
    if (inheritInfo != null && !inheritInfo.isRoot()) {
      DeployBeanInfo<?> rootBeanInfo = rootInfoMap.get(inheritInfo.getRoot().getType());
      PlatformIdGenerator rootIdGen = rootBeanInfo.getDescriptor().getIdGenerator();
      if (rootIdGen != null) {
        descriptor.setIdGenerator(rootIdGen);
      }
    }
  }

  protected void setBeanReflect(DeployBeanDescriptor<?> desc) {
    // Set the BeanReflectGetter and BeanReflectSetter that typically
    // use generated code. NB: Due to Bug 166 so now doing this for
    // abstract classes as well.
    boolean supportCustom = ObjectEntity.class.isAssignableFrom(desc.getBeanType());
    if (!supportCustom) {
      return;
    }
    DeployBeanProperty customPropMap = null;
    for (DeployBeanProperty prop : desc.propertiesAll()) {
      if (prop.getName().equals(ObjectEntity.KEY_CUSTOM) && prop.isTransient()) {
        customPropMap = prop;
        break;
      }
    }
    if (customPropMap == null) {
      return;
    }
    BiConsumerPropertyAccess customAccess = new BiConsumerPropertyAccess();
    for (DeployBeanProperty prop : desc.propertiesAll()) {
      if (prop.getField() == null) {//is custom
        int customPos = customAccess.addProperties(prop.getName());
        Object getterSetter = customAccess.getGetter(customPos);
        prop.setPropertyIndex(customPropMap.getPropertyIndex());
        prop.setGetter((BeanPropertyGetter) getterSetter);
        prop.setSetter((BeanPropertySetter) getterSetter);
      }
    }
    desc.removeProperty(customPropMap);
  }

  protected void readEntityRelationships(DeployBeanInfo<?> info) {
    // We only perform 'circular' checks etc after we have
    // all the DeployBeanDescriptors created and in the map.
    List<DeployBeanPropertyAssocOne<?>> primaryKeyJoinCheck = new ArrayList<>();

    beanDescriptorManager.checkMappedBy(info, primaryKeyJoinCheck);

    for (DeployBeanPropertyAssocOne<?> prop : primaryKeyJoinCheck) {
      beanDescriptorManager.checkUniDirectionalPrimaryKeyJoin(prop);
    }

    beanDescriptorManager.secondaryPropsJoins(info);

    beanDescriptorManager.setInheritanceInfo(info);
  }

  protected void initialiseAll(List<BeanDescriptor<?>> descMap) {
    // now that all the BeanDescriptors are in their map
    // we can initialise them which sorts out circular
    // dependencies for OneToMany and ManyToOne etc
    BeanDescriptorInitContext initContext = beanDescriptorManager.initContext;

    // PASS 1:
    // initialise the ID properties of all the beans
    // first (as they are needed to initialise the
    // associated properties in the second pass).
    for (BeanDescriptor<?> d : descMap) {
      d.initialiseId(initContext);
    }

    // PASS 2:
    // now initialise all the inherit info
    for (BeanDescriptor<?> d : descMap) {
      d.initInheritInfo();
    }

    // PASS 3:
    // now initialise all the associated properties
    for (BeanDescriptor<?> d : descMap) {
      // also look for intersection tables with
      // associated history support and register them
      // into the asOfTableMap
      d.initialiseOther(initContext);
    }

    // PASS 4:
    // now initialise document mapping which needs target descriptors
    for (BeanDescriptor<?> d : descMap) {
      d.initialiseDocMapping();
    }

    // create BeanManager for each non-embedded entity bean
    for (BeanDescriptor<?> d : descMap) {
      d.initLast();
      if (!d.isEmbedded()) {
//        beanManagerMap.put(d.fullName(), beanManagerFactory.create(d));
//        checkForValidEmbeddedId(d);
      }
    }
  }

  protected void readTableToDescriptor(BeanDescriptor<?> desc) {
    String baseTable = desc.baseTable();
    if (baseTable != null) {
      baseTable = baseTable.toLowerCase();
      List<BeanDescriptor<?>> list = tableToDescMap.computeIfAbsent(baseTable, k -> new ArrayList<>(1));
      list.add(desc);
    }
    if (desc.entityType() == BeanDescriptor.EntityType.VIEW && desc.isQueryCaching()) {
      // build map of tables to view entities dependent on those tables
      // for the purpose of invalidating appropriate query caches
      String[] dependentTables = desc.dependentTables();
      if (dependentTables != null && dependentTables.length > 0) {
        for (String depTable : dependentTables) {
          depTable = depTable.toLowerCase();
          List<BeanDescriptor<?>> list = tableToViewDescMap.computeIfAbsent(depTable, k -> new ArrayList<>(1));
          list.add(desc);
        }
      }
    }
  }

  public BeanDescriptor<?> createBeanDescriptor(Class<?> beanClass, XEntity entity) throws Exception {
    lock.lock();
    try {
      BeanDescriptorMapContext context = new BeanDescriptorMapContext(beanTableMap, descMap, descInfoMap);
      BeanDescriptorMapTemporal factory = new BeanDescriptorMapTemporal(beanDescriptorManager, context, readAnnotations, createProperties, rootInfoMap);
      deployInstance.set(factory);
      DeployBeanInfo<?> info = rootInfoMap.get(beanClass);
      DeployBeanInfo<?> newInfo = createProperties.createDeployBeanInfo(entity, beanClass, info, readAnnotations, factory);
      BeanDescriptor<?> desc = factory.deployInfo(newInfo);
      return desc;
    } finally {
      deployInstance.set(null);
      lock.unlock();
    }
  }
}
