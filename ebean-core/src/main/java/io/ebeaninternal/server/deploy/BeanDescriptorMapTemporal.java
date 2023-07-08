package io.ebeaninternal.server.deploy;

import io.ebean.bean.ObjectEntity;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanDescriptorMapTemporal implements BeanDescriptorMap {
  protected static final Logger log = CoreLog.internal;
  private final BeanDescriptorManager proxy;
  private final BeanDescriptorMapCheck mapCheck;
  private final BeanDescriptorMapContext context;
  protected final Map<String, BeanDescriptor<?>> descMap = new HashMap<>();
  protected final Map<Class<?>, DeployBeanInfo<?>> descInfoMap = new HashMap<>();
  protected final Map<String, BeanTable> beanTableMap = new HashMap<>();
  protected final Map<String, BeanManager<?>> beanManagerMap = new HashMap<>();
  protected final Map<String, BeanTable> beanTableCache = new HashMap<>();

  protected final Map<String, List<BeanDescriptor<?>>> tableToDescMap = new HashMap<>();
  protected final Map<String, List<BeanDescriptor<?>>> tableToViewDescMap = new HashMap<>();

  protected final XReadAnnotations readAnnotations;
  protected final TenantDeployCreateProperties createProperties;

  protected final BeanDescriptorInitContext initContext;
  private final List<BeanDescriptor<?>> descriptors = new ArrayList<>();

  public BeanDescriptorMapTemporal(BeanDescriptorManagerTenant proxy, BeanDescriptorMapContext context, XReadAnnotations readAnnotations, TenantDeployCreateProperties createProperties) {
    this.proxy = proxy;
    this.context = context;
    this.readAnnotations = readAnnotations;
    this.createProperties = createProperties;
    this.mapCheck = new BeanDescriptorMapCheck(proxy.typeManager, this);
    this.initContext = new BeanDescriptorInitContext(new HashMap<>(), new HashMap<>(), proxy.asOfViewSuffix);
  }

  @Override
  public String name() {
    return proxy.name();
  }

  @Override
  public DatabaseConfig config() {
    return proxy.config();
  }

  @Override
  public SpiCacheManager cacheManager() {
    return proxy.cacheManager();
  }

  @Override
  public NamingConvention namingConvention() {
    return proxy.namingConvention();
  }

  @Override
  public boolean isMultiValueSupported() {
    return proxy.isMultiValueSupported();
  }

  protected boolean isDeploying(Class<?> entityClass) {
    return context.isDeploying(entityClass) || beanTableMap.containsKey(entityClass.getName());
  }

  protected boolean isDeployed(Class<?> entityClass) {
    return context.isDeployed(entityClass) || descMap.containsKey(entityClass.getName());
  }

  protected void deploy(Class<?> entityClass) {
    deploy(entityClass, null);
  }

  protected void deploy(Class<?> entityClass, XEntity entity) {
    if (isDeploying(entityClass)) {
      return;
    }
    deployLocked(entityClass, entity);
  }

  protected void deployLocked(Class<?> entityClass, XEntity entity) {
    deployInternal(entityClass, entity, descriptors);
  }

  public void initialise() {
    initialise(descriptors);
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

  protected void deployInternal(Class<?> entityClass, XEntity entity, List<BeanDescriptor<?>> descriptors) {
    //1.createListeners();
    //2.readEntityDeploymentInitial
    DeployInfo info = createDeployBeanInfo(entityClass, entity);
    if (info.initialise()) {
      return;
    }
    deployInternal(info.info, descriptors);
  }

  protected void deployInternal(DeployBeanInfo<?> info, List<BeanDescriptor<?>> descriptors) {
    //1.createListeners();
    //2.readEntityDeploymentInitial
    deploy(info);

    BeanDescriptor<?> desc = registerDescriptor(info);
    descriptors.add(desc);
  }

  protected BeanDescriptor<?> registerDescriptor(DeployBeanInfo<?> info) {
    BeanDescriptor<?> desc = new BeanDescriptor<>(this, info.getDescriptor());
    descMap.put(desc.type().getName(), desc);
    /*
    if (desc.isDocStoreMapped()) {
      descQueueMap.put(desc.docStoreQueueId(), desc);
    }
    for (BeanPropertyAssocMany<?> many : desc.propertiesMany()) {
      if (many.isElementCollection()) {
        elementDescriptors.add(many.elementDescriptor());
      }
    }*/
    return desc;
  }

  protected void initialise(List<BeanDescriptor<?>> descriptors) {
    //7.initialiseAll
    initialiseAll(descriptors);

    for (BeanDescriptor<?> desc : descriptors) {
      //8.readForeignKeys
      readForeignKeys(desc);
      //9.readTableToDescriptor();
      readTableToDescriptor(desc);
    }
  }

  protected void readForeignKeys(BeanDescriptor<?> desc) {
    desc.initialiseFkeys();
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

  protected void readInheritedIdGenerators(DeployBeanInfo<?> info) {
    DeployBeanDescriptor<?> descriptor = info.getDescriptor();
    InheritInfo inheritInfo = descriptor.getInheritInfo();
    if (inheritInfo != null && !inheritInfo.isRoot()) {
      DeployBeanInfo<?> rootBeanInfo = descInfo(inheritInfo.getRoot().getType());
      PlatformIdGenerator rootIdGen = rootBeanInfo.getDescriptor().getIdGenerator();
      if (rootIdGen != null) {
        descriptor.setIdGenerator(rootIdGen);
      }
    }
  }

  protected void readEntityRelationships(DeployBeanInfo<?> info) {
    // We only perform 'circular' checks etc after we have
    // all the DeployBeanDescriptors created and in the map.
    List<DeployBeanPropertyAssocOne<?>> primaryKeyJoinCheck = new ArrayList<>();

    mapCheck.checkMappedBy(info, primaryKeyJoinCheck);

    for (DeployBeanPropertyAssocOne<?> prop : primaryKeyJoinCheck) {
      mapCheck.checkUniDirectionalPrimaryKeyJoin(prop);
    }

    mapCheck.secondaryPropsJoins(info);

    mapCheck.setInheritanceInfo(info);
  }

  protected void initialiseAll(List<BeanDescriptor<?>> descMap) {
    // now that all the BeanDescriptors are in their map
    // we can initialise them which sorts out circular
    // dependencies for OneToMany and ManyToOne etc

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
    proxy.transientProperties.process(desc);
    proxy.setScalarType(desc);
    if (!desc.isEmbedded()) {
      // Set IdGenerator or use DB Identity
      proxy.setIdGeneration(desc);
      // find the appropriate default concurrency mode
      proxy.setConcurrencyMode(desc);
    }
    // generate the byte code
    setBeanReflect(desc);
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

  protected <T> BeanDescriptor<T> deployInfo(Class<T> beanClass, DeployBeanInfo<?> info) {
    deployInternal(info, descriptors);
    return getDesc(beanClass);
  }

  private <T> BeanDescriptor<T> getDesc(Class<T> beanClass) {
    return (BeanDescriptor<T>) descMap.get(beanClass.getName());
  }

  public <T> BeanDescriptor<T> desc(Class<T> beanClass) {
    BeanDescriptor<T> desc = context.desc(beanClass);
    if (desc != null) {
      return desc;
    }
    return (BeanDescriptor<T>) descMap.get(beanClass.getName());
  }

  public DeployBeanInfo<?> descInfo(Class<?> beanClass) {
    return context.getDescInfo(beanClass, this::getDescInfo);
  }

  private DeployBeanInfo<?> getDescInfo(Class<?> beaenClass) {
    return descInfoMap.get(beaenClass);
  }

  @Override
  public <T> BeanDescriptor<T> descriptor(Class<T> beanClass) {
    if (!isDeployed(beanClass)) {
      deploy(beanClass);
    }
    BeanDescriptor<T> desc = this.desc(beanClass);
    if (desc == null) {
      throw new IllegalStateException(String.format("%s bean descriptor is null", beanClass.getName()));
    }
    return desc;
  }


  @Override
  public EncryptKey encryptKey(String tableName, String columnName) {
    return proxy.encryptKey(tableName, columnName);
  }

  @Override
  public IdBinder createIdBinder(BeanProperty id) {
    return proxy.createIdBinder(id);
  }

  @Override
  public <T> DocStoreBeanAdapter<T> createDocStoreBeanAdapter(BeanDescriptor<T> descriptor, DeployBeanDescriptor<T> deploy) {
    return proxy.createDocStoreBeanAdapter(descriptor, deploy);
  }

  @Override
  public ScalarType<?> scalarType(int jdbcType) {
    return proxy.scalarType(jdbcType);
  }

  @Override
  public ScalarType<?> scalarType(String cast) {
    return proxy.scalarType(cast);
  }

  @Override
  public boolean isJacksonCorePresent() {
    return proxy.isJacksonCorePresent();
  }

  @Override
  public boolean isTableManaged(String tableName) {
    return proxy.isTableManaged(tableName);
  }

  @Override
  public BeanTable beanTable(Class<?> type) {
    BeanTable table = getBeanTable(type);
    if (table == null) {
      table = createBeanTable(type);
      if (table == null) {
        throw new IllegalStateException(String.format("%s bean table is null", type.getName()));
      }
    }
    return table;
  }

  protected BeanTable getBeanTable(Class<?> type) {
    BeanTable table = context.beanTable(type);
    if (table == null) {
      table = beanTableMap.get(type.getName());
    }
    return table;
  }

  protected BeanTable createBeanTable(Class<?> beanClass) {
    DeployInfo info = createDeployBeanInfo(beanClass);
    info.initialise();
    return createBeanTable(info.info);
  }


  protected BeanTable readEntityBeanTable(DeployBeanInfo<?> info) {
    BeanTable beanTable = getBeanTable(info.getDescriptor().getBeanType());
    if (beanTable != null) {
      return beanTable;
    }
    beanTable = createBeanTable(info);
    beanTableMap.put(beanTable.getBeanType().getName(), beanTable);
    return beanTable;
  }

  protected BeanTable createBeanTable(DeployBeanInfo<?> info) {
    String name = info.getDescriptor().getBeanType().getName();
    return beanTableCache.computeIfAbsent(name, (e) -> {
      DeployBeanDescriptor<?> deployDescriptor = info.getDescriptor();
      DeployBeanTable beanTable = deployDescriptor.createDeployBeanTable();
      return new BeanTable(beanTable, this);
    });
  }

  protected void registerBeanManager(BeanManager beanManager) {
    Class<?> beanClass = beanManager.getBeanDescriptor().beanType;
    descMap.put(beanClass.getName(), beanManager.getBeanDescriptor());
    beanManagerMap.put(beanClass.getName(), beanManager);
  }

  protected DeployInfo createDeployBeanInfo(final Class<?> beanClass) {
    return createDeployBeanInfo(beanClass, null);
  }

  protected DeployInfo createDeployBeanInfo(final Class<?> beanClass, final XEntity entity) {
    if (entity == null && descInfoMap.containsKey(beanClass)) {
      return new DeployInfo(beanClass, descInfoMap.get(beanClass), null);
    }
    DeployBeanInfo<?> info = context.getRootDescInfo(beanClass); //这里是从父级集成来的

    if (info == null) {
      throw new IllegalStateException(
        "If you are running in an IDE with enhancement plugin try a Build -> Rebuild Project to recompile and enhance all entity beans. " +
          "Error - for type " + beanClass);
    }
    try {
      DeployBeanInfo<?> newInfo = createProperties.createDeployBeanInfo(entity, beanClass, info, readAnnotations, this);
      if (newInfo == info) { //Class的属性没有发生变化,部署的是同一个
        BeanManager beanManager = proxy.beanManager(beanClass.getName());
        return new DeployInfo(beanClass, newInfo, beanManager);
      }
      return new DeployInfo(beanClass, newInfo, null);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private class DeployInfo {
    protected final Class<?> beanClass;
    protected final DeployBeanInfo<?> info;
    protected final BeanManager beanManager;

    public DeployInfo(final Class<?> beanClass, DeployBeanInfo<?> info, BeanManager beanManager) {
      this.beanClass = beanClass;
      this.info = info;
      this.beanManager = beanManager;
    }

    public boolean initialise() {
      if (beanManager != null) {
        registerBeanManager(beanManager);
        return true;
      } else {
        descInfoMap.put(beanClass, info);
        return false;
      }
    }
  }
}
