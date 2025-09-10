package io.ebeaninternal.server.deploy;

import io.ebean.bean.ElementBean;
import io.ebean.bean.EntityBean;
import io.ebean.bean.InterceptReadWrite;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebeaninternal.server.deploy.meta.*;
import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;
import io.ebeaninternal.server.deploy.parse.TenantDeployCreateProperties;
import io.ebeaninternal.server.deploy.parse.XReadAnnotations;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeaninternal.server.properties.BeanElementPropertyAccess;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class BeanDescriptorMapTemporal {
  private final Object tenantId;
  private final BeanDescriptorManager proxy;
  private final BeanDescriptorMapTenantProxy proxyMap;
  private final BeanDescriptorMapCheck mapCheck;
  private final BeanDescriptorMapContext context;
  protected final List<BeanDescriptor<?>> elementDescriptors = new LinkedList<>();
  protected final Map<String, BeanDescriptor<?>> descMap = new HashMap<>();
  protected final Map<String, BeanDescriptor<?>> descQueueMap = new HashMap<>();
  protected final Map<Class<?>, DeployBeanInfo<?>> descInfoMap = new HashMap<>();
  protected final List<BeanDescriptorConsumer> descListeners = new LinkedList<>();
  protected final Map<String, BeanTable> beanTableMap = new HashMap<>();
  protected final Map<String, BeanManager<?>> beanManagerMap = new HashMap<>();
  protected final Map<String, BeanTable> beanTableCache = new HashMap<>();

  protected final Map<String, List<BeanDescriptor<?>>> tableToDescMap = new HashMap<>();
  protected final Map<String, List<BeanDescriptor<?>>> tableToViewDescMap = new HashMap<>();

  protected final XReadAnnotations readAnnotations;
  protected final TenantDeployCreateProperties createProperties;

  protected final BeanDescriptorInitContext initContext;
  private final List<BeanDescriptor<?>> descriptors = new ArrayList<>();
  private final Map<String, String> withHistoryTables = new HashMap<>();
  private final Map<String, String> draftTables = new HashMap<>();

  public BeanDescriptorMapTemporal(Object tenantId, BeanDescriptorManagerTenant proxy, BeanDescriptorMapContext context, XReadAnnotations readAnnotations, TenantDeployCreateProperties createProperties) {
    this.tenantId = tenantId;
    this.proxy = proxy;
    this.proxyMap = new BeanDescriptorMapTenantProxy(proxy, this);
    this.context = context;
    this.readAnnotations = readAnnotations;
    this.createProperties = createProperties;
    this.mapCheck = new BeanDescriptorMapCheck(proxy.typeManager, this);
    this.initContext = new BeanDescriptorInitContext(withHistoryTables, draftTables, proxy.asOfViewSuffix);
  }

  protected boolean isDeploying(Class<?> entityClass) {
    return context.isDeploying(entityClass) || beanTableMap.containsKey(entityClass.getName()) || descMap.containsKey(entityClass.getName());
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
    if (!info.initialise()) {
      deployInternal(info.info, descriptors);
    }
  }

  protected void deployInternal(DeployBeanInfo<?> info, List<BeanDescriptor<?>> descriptors) {
    //1.createListeners();
    //2.readEntityDeploymentInitial
    deploy(info);

    registerDescriptor(info, descriptors);
  }

  protected BeanDescriptor<?> registerDescriptor(DeployBeanInfo<?> info, List<BeanDescriptor<?>> descriptors) {
    BeanDescriptor<?> desc = new BeanDescriptor<>(proxyMap, info.getDescriptor());
    descMap.put(desc.type().getName(), desc);
    if (desc.isDocStoreMapped()) {
      descQueueMap.put(desc.docStoreQueueId(), desc);
    }
    for (BeanPropertyAssocMany<?> many : desc.propertiesMany()) {
      if (many.isElementCollection()) {
        elementDescriptors.add(many.elementDescriptor());
      }
    }
    descriptors.add(desc);
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
    }
  }

  protected <T> void readDeployAssociations(DeployBeanDescriptor<T> desc) {
    for (DeployBeanProperty prop : desc.propertiesAssocOne()) {
      DeployBeanPropertyAssoc assoc = (DeployBeanPropertyAssoc) prop;
      if (assoc.isId() || assoc.isTransient() || assoc.isEmbedded()) {
        continue;
      }
      deploy(assoc.getTargetType());
    }
    for (DeployBeanProperty prop : desc.propertiesAssocMany()) {
      DeployBeanPropertyAssocMany assoc = (DeployBeanPropertyAssocMany) prop;
      if (assoc.isId() || assoc.isTransient() || assoc.isEmbedded() || assoc.isElementCollection()) {
        continue;
      }
      deploy(assoc.getTargetType());
    }
  }

  protected <T> void readDeployAssociations(DeployBeanInfo<T> info) {
    DeployBeanDescriptor<T> desc = info.getDescriptor();

    readAnnotations.readAssociations(info, proxyMap);

    readDeployAssociations(desc);

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
    DeployBeanProperty[] customSlot = desc.readCustomSlot();
    if (customSlot.length == 0) {
      return;
    }
    DeployBeanProperty ccp = customSlot[0];
    DeployBeanProperty slot = customSlot[1];
    ccp.setTransient();
    ccp.setEmbedded();
    ccp.setUnmappedJson();
    ccp.setScalarType(proxy.typeManager.dbMapType());
    slot.setTransient();
    slot.setJsonSerialize(false);

    Map<String, Integer> propMap = new HashMap<>();
    List<DeployBeanProperty> properties = new ArrayList<>();
    final int propertyIndex = ccp.getPropertyIndex();
    final int slotIndex = slot.getPropertyIndex();
    for (DeployBeanProperty prop : desc.propertiesAll()) {
      if (prop.getField() == null) {//is custom
        properties.add(prop);
      }
    }
    String[] propertiesName = new String[properties.size()];
    for (int i = 0; i < properties.size(); i++) {
      propertiesName[i] = properties.get(i).getName();
      propMap.put(propertiesName[i], i);
    }
    propMap = Collections.unmodifiableMap(propMap);
    Function<EntityBean, EntityBean> elementBean = elementBeanSupplier(propertyIndex, slotIndex, propertiesName, propMap);
    for (int i = 0; i < properties.size(); i++) {
      DeployBeanProperty prop = properties.get(i);
      BeanElementPropertyAccess access = new BeanElementPropertyAccess(elementBean, i, prop.getScalarType());
      prop.setPropertyIndex(slotIndex);
      prop.setFieldIndex(i);
      prop.setGetter(access);
      prop.setSetter(access);
      if (prop.isAggregation()) {
        prop.setAggregationPrefix(DetermineAggPath.manyPath(prop.getRawAggregation(), desc));
      }
    }
    desc.setElementBean(elementBean);
  }

  /**
   * supply an element bean
   *
   * @param elementFieldIndex
   * @param slotIndex
   * @param properties
   * @param propMap
   * @return
   */
  private Function<EntityBean, EntityBean> elementBeanSupplier(int elementFieldIndex, int slotIndex, String[] properties, Map<String, Integer> propMap) {
    return (bean) -> {
      ElementBean element = (ElementBean) bean._ebean_getField(elementFieldIndex);
      if (element._ebean_getPropertyNames() != properties) {
        element._ebean_setInterceptProperties(properties, propMap);
        element._ebean_setIntercept(new BeanElementPropertyIntercept(new InterceptReadWrite(element), bean, elementFieldIndex, slotIndex));
      }
      return element;
    };
  }

  protected <T> BeanDescriptor<T> deployInfo(Class<T> beanClass, DeployInfo info) {
    if (!info.initialise()) {
      deployInternal(info.info, descriptors);
    }
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

  private DeployBeanInfo<?> getDescInfo(Class<?> beanClass) {
    return descInfoMap.get(beanClass);
  }

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
    Class<?> beanType = info.getDescriptor().getBeanType();
    BeanTable beanTable = getBeanTable(beanType);
    if (beanTable == null) {
      beanTable = createBeanTable(info);
    }
    beanTableMap.put(beanType.getName(), beanTable);
    return beanTable;
  }

  protected BeanTable createBeanTable(DeployBeanInfo<?> info) {
    String name = info.getDescriptor().getBeanType().getName();
    return beanTableCache.computeIfAbsent(name, (e) -> {
      DeployBeanDescriptor<?> deployDescriptor = info.getDescriptor();
      DeployBeanTable beanTable = deployDescriptor.createDeployBeanTable();
      return new BeanTable(beanTable, proxyMap);
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
      DeployBeanInfo<?> newInfo = createProperties.createDeployBeanInfo(tenantId, beanClass, entity, info, readAnnotations);
      if (newInfo == info) { //Class的属性没有发生变化,部署的是同一个
        return new DeployInfo(beanClass, newInfo, proxy.beanManager(beanClass.getName()));
      }
      return new DeployInfo(beanClass, newInfo, null);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public <T> void listenDescriptor(Class<?> entityType, Class<T> targetClass, Consumer<BeanDescriptor<T>> consumer) {
    descListeners.add(new BeanDescriptorConsumer<>(entityType, targetClass, consumer));
  }

  protected void clear() {
    for (DeployBeanInfo<?> value : descInfoMap.values()) {
      value.clear();
    }
    proxyMap.clear();
    elementDescriptors.clear();
    descMap.clear();
    descQueueMap.clear();
    descInfoMap.clear();
    descListeners.clear();
    beanTableMap.clear();
    beanManagerMap.clear();
    beanTableCache.clear();
    tableToDescMap.clear();
    tableToViewDescMap.clear();
    descriptors.clear();
    withHistoryTables.clear();
    draftTables.clear();
  }

  protected class DeployInfo {
    protected final Class<?> beanClass;
    protected final DeployBeanInfo<?> info;
    protected final BeanManager<?> beanManager;

    public DeployInfo(final Class<?> beanClass, DeployBeanInfo<?> info, BeanManager<?> beanManager) {
      this.beanClass = beanClass;
      this.info = info;
      this.beanManager = beanManager;
    }

    public boolean initialise() {
      if (beanManager != null) {
        if (info.getDescriptor().readCustomSlot().length == 0) {//not a dynamic bean
          registerBeanManager(beanManager);
        } else {
          registerDescriptor(info, descriptors); //a dynamic bean that to deploy associations
          readDeployAssociations(info.getDescriptor());
        }
        return true;
      } else {
        descInfoMap.put(beanClass, info);
        return false;
      }
    }
  }
}
