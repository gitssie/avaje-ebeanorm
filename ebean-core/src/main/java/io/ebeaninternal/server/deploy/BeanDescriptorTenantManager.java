package io.ebeaninternal.server.deploy;

import io.ebean.config.BeanNotEnhancedException;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.core.InternalConfiguration;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;
import io.ebeaninternal.server.deploy.parse.ReadAnnotations;
import io.ebeaninternal.server.deploy.parse.TenantDeployCreateProperties;
import io.ebeaninternal.server.deploy.parse.XReadAnnotations;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeaninternal.server.properties.BeanPropertiesReader;
import io.ebeaninternal.server.properties.BeanPropertyGetter;
import io.ebeaninternal.server.properties.BeanPropertySetter;
import io.ebeaninternal.server.properties.BiConsumerPropertyAccess;
import io.ebeaninternal.xmapping.api.XmapEbean;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Creates Tenanted BeanDescriptors.
 */
public class BeanDescriptorTenantManager extends BeanDescriptorManager {
//  protected List<BeanDescriptor<?>> elementDescriptors = new ArrayList<>();
//  protected Map<Class<?>, BeanTable> beanTableMap = new HashMap<>();
//  protected Map<String, BeanDescriptor<?>> descMap = new HashMap<>();
//  protected Map<String, BeanDescriptor<?>> descQueueMap = new HashMap<>();
//  protected Map<String, BeanManager<?>> beanManagerMap = new HashMap<>();
//  protected Map<String, List<BeanDescriptor<?>>> tableToDescMap = new HashMap<>();
//  protected Map<String, List<BeanDescriptor<?>>> tableToViewDescMap = new HashMap<>();

  protected final XReadAnnotations readAnnotations;
  protected final TenantDeployCreateProperties tenantCreateProperties;
  protected final BeanDescriptorInitContext initContext;

  protected SpiEbeanServer ebeanServer;
  /**
   * Create for a given database dbConfig.
   */
  public BeanDescriptorTenantManager(InternalConfiguration config) {
    super(config);
    String versionsBetweenSuffix = versionsBetweenSuffix(databasePlatform, this.config);
    this.readAnnotations = new XReadAnnotations(config.getGeneratedPropertyFactory(), asOfViewSuffix, versionsBetweenSuffix, this.config);
    this.tenantCreateProperties = (TenantDeployCreateProperties) createProperties;
    this.initContext = new BeanDescriptorInitContext(asOfTableMap, draftTableMap, asOfViewSuffix);

  }

  /**
   * Deploy returning the asOfTableMap (which is required by the SQL builders).
   */
  @Override
  public Map<String, String> deploy(List<XmapEbean> mappings) {
    try {
      createListeners();
//      readEntityDeploymentInitial();
//      readXmlMapping(mappings);
      readEntityBeanTable();
      readEntityDeploymentAssociations();
      readInheritedIdGenerators();
      // creates the BeanDescriptors
      readEntityRelationships();
      List<BeanDescriptor<?>> list = new ArrayList<>(descMap.values());
      list.sort(beanDescComparator);
      immutableDescriptorList = Collections.unmodifiableList(list);
      initialiseAll();
      readForeignKeys();
      readTableToDescriptor();
      logStatus();

      // clear collections we no longer need
      embeddedIdTypes = null;
      embeddedBeans = null;
      deployInfoMap = null;
      return asOfTableMap;
    } catch (BeanNotEnhancedException e) {
      throw e;
    } catch (RuntimeException e) {
      log.error("Error in deployment", e);
      throw e;
    }
  }
  @Override
  protected void readEntityDeploymentInitial() {
    for (Class<?> entityClass : bootupClasses.getEntities()) {
      DeployBeanInfo<?> info = createDeployBeanInfo(entityClass);
      deployInfoMap.put(entityClass, info);
      Class<?> embeddedIdType = info.getEmbeddedIdType();
      if (embeddedIdType != null) {
        embeddedIdTypes.add(embeddedIdType);
      }
    }
    for (Class<?> entityClass : bootupClasses.getEmbeddables()) {
      DeployBeanInfo<?> info = createDeployBeanInfo(entityClass);
      deployInfoMap.put(entityClass, info);
      if (embeddedIdTypes.contains(entityClass)) {
        // register embeddedId types early - scalar properties only
        // and needed for creating BeanTables (id properties)
        registerEmbeddedBean(info);
      } else {
        // delay register of other embedded beans until after
        // the BeanTables have been created to support ManyToOne
        embeddedBeans.add(info);
      }
    }
  }

  /**
   * 注册实体
   * @param entityClass
   */
  public void registerEntity(Class<?> entityClass) {
    DeployBeanInfo<?> info = createDeployBeanInfo(entityClass);
//    deployInfoMap.put(entityClass, info);
    Class<?> embeddedIdType = info.getEmbeddedIdType();
    if (embeddedIdType != null) {
//      embeddedIdTypes.add(embeddedIdType);
    }
    createByteCode(info.getDescriptor()); //初始化Getter Setter
    registerDescriptor(info); //注册Mapper
  }

  @Override
  protected void registerDescriptor(DeployBeanInfo<?> info) {
    BeanDescriptor<?> desc = new BeanDescriptor<>(this, info.getDescriptor());
    desc.setEbeanServer(ebeanServer);

    initialise(desc);

    descMap.put(desc.type().getName(), desc);
    if (desc.isDocStoreMapped()) {
      descQueueMap.put(desc.docStoreQueueId(), desc);
    }
    for (BeanPropertyAssocMany<?> many : desc.propertiesMany()) {
      if (many.isElementCollection()) {
        elementDescriptors.add(many.elementDescriptor());
      }
    }
  }
  @Override
  protected <T> DeployBeanInfo<T> createDeployBeanInfo(Class<T> beanClass) {
    DeployBeanDescriptor<T> desc = new DeployBeanDescriptor<>(this, beanClass, config);

    beanLifecycleAdapterFactory.addLifecycleMethods(desc);
    // set bean controller, finder and listener
    setBeanControllerFinderListener(desc);
    deplyInherit.process(desc);
    desc.checkInheritanceMapping();

    DeployBeanInfo<T> info = tenantCreateProperties.createDeployBeanInfo(deployUtil, desc,readAnnotations);

    return info;
  }

  @Override
  public void setEbeanServer(SpiEbeanServer internalEbean) {
    super.setEbeanServer(internalEbean);
    ebeanServer = internalEbean;
  }

  @Override
  protected void setBeanReflect(DeployBeanDescriptor<?> desc) {
    // Set the BeanReflectGetter and BeanReflectSetter that typically
    // use generated code. NB: Due to Bug 166 so now doing this for
    // abstract classes as well.
    BeanPropertiesReader reflectProps = new BeanPropertiesReader(desc.getBeanType());
    boolean supportCustom = BiConsumer.class.isAssignableFrom(desc.getBeanType()) && Function.class.isAssignableFrom(desc.getBeanType());
    BiConsumerPropertyAccess customAccess = null;
    for (DeployBeanProperty prop : desc.propertiesAll()) {
      String propName = prop.getName();
      Integer pos = reflectProps.getPropertyIndex(propName);
      if (pos == null) {
        if(supportCustom){
          if(customAccess == null){
            customAccess = new BiConsumerPropertyAccess(reflectProps.getProperties().length);
          }
          int customPos = customAccess.addProperties(propName);
          Object getterSetter = customAccess.getGetter(customPos);
          prop.setPropertyIndex(customPos);
          prop.setGetter((BeanPropertyGetter)getterSetter);
          prop.setSetter((BeanPropertySetter) getterSetter);
        }else if (isPersistentField(prop)) {
          throw new IllegalStateException(
            "If you are running in an IDE with enhancement plugin try a Build -> Rebuild Project to recompile and enhance all entity beans. " +
              "Error - property " + propName + " not found in " + propName + " for type " + desc.getBeanType());
        }
      } else {
        final int propertyIndex = pos;
        prop.setPropertyIndex(propertyIndex);
        prop.setGetter(beanPropertyAccess.getGetter(propertyIndex));
        prop.setSetter(beanPropertyAccess.getSetter(propertyIndex));
        if (prop.isAggregation()) {
          prop.setAggregationPrefix(DetermineAggPath.manyPath(prop.getRawAggregation(), desc));
        }
      }
    }
  }

  /**
   * 初始化 BeanDescriptor
   * Initialise all the BeanDescriptors.
   * <p>
   * This occurs after all the BeanDescriptors have been created. This resolves
   * circular relationships between BeanDescriptors.
   * <p>
   * Also responsible for creating all the BeanManagers which contain the
   * persister, listener etc.
   */
  protected void initialise(BeanDescriptor<?> d) {
    // now that all the BeanDescriptors are in their map
    // we can initialise them which sorts out circular
    // dependencies for OneToMany and ManyToOne etc

    // PASS 1:
    // initialise the ID properties of all the beans
    // first (as they are needed to initialise the
    // associated properties in the second pass).
    d.initialiseId(initContext);

    // PASS 2:
    // now initialise all the inherit info
    d.initInheritInfo();

    // PASS 3:
    // now initialise all the associated properties
    d.initialiseOther(initContext);

    // PASS 4:
    // now initialise document mapping which needs target descriptors
    d.initialiseDocMapping();

    // create BeanManager for each non-embedded entity bean
    d.initLast();
    if (!d.isEmbedded()) {
      beanManagerMap.put(d.fullName(), beanManagerFactory.create(d));
      checkForValidEmbeddedId(d);
    }
  }
}
