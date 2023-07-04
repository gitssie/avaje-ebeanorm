package io.ebeaninternal.server.deploy;

import io.ebean.bean.XEntityProvider;
import io.ebean.config.CurrentTenantProvider;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.core.InternalConfiguration;
import io.ebeaninternal.server.core.ServiceUtil;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;
import io.ebeaninternal.server.deploy.parse.TenantDeployCreateProperties;
import io.ebeaninternal.server.deploy.parse.XReadAnnotations;

import javax.persistence.PersistenceException;

public class BeanDescriptorManagerTenant extends BeanDescriptorManager {
  protected final XReadAnnotations readAnnotations;
  protected final TenantDeployCreateProperties tenantCreateProperties;
  protected final BeanDescriptorInitContext initContext;
  protected final CurrentTenantProvider tenantProvider;
  protected final BeanDescriptorManagerProvider beanDescriptorManagerProvider;
  protected SpiEbeanServer ebeanServer;

  public BeanDescriptorManagerTenant(InternalConfiguration config) {
    super(config);
    this.tenantProvider = config.getConfig().getCurrentTenantProvider();
    String versionsBetweenSuffix = versionsBetweenSuffix(databasePlatform, this.config);
    this.readAnnotations = new XReadAnnotations(config.getGeneratedPropertyFactory(), asOfViewSuffix, versionsBetweenSuffix, this.config);
    XEntityProvider entityProvider = (XEntityProvider) config.getConfig().getServiceObject(XEntityProvider.class.getName());
    this.tenantCreateProperties = new TenantDeployCreateProperties(createProperties, entityProvider.create());
    this.initContext = new BeanDescriptorInitContext(asOfTableMap, draftTableMap, asOfViewSuffix);
    this.beanDescriptorManagerProvider = new BeanDescriptorManagerProvider(this, tenantProvider);
  }

  protected <S> S service(Class<S> cls) {
    return ServiceUtil.service(cls);
  }

  @Override
  public <T> BeanDescriptor<T> descriptor(Class<T> entityType) {
    Object tenantId = tenantProvider.currentId();
    if (ebeanServer == null || tenantId == null) {
      return super.descriptor(entityType);
    }
    BeanDescriptorMapTenant mapTenant = beanDescriptorManagerProvider.getDescriptorTenant(tenantId);
    return mapTenant.descriptor(entityType);
  }

  @Override
  public <T> BeanManager<T> beanManager(Class<T> entityType) {
    Object tenantId = tenantProvider.currentId();
    if (ebeanServer == null || tenantId == null) {
      return super.beanManager(entityType);
    }
    BeanDescriptorMapTenant mapTenant = beanDescriptorManagerProvider.getDescriptorTenant(tenantId);
    return mapTenant.beanManager(entityType);
  }

  @Override
  public void setEbeanServer(SpiEbeanServer internalEbean) {
    super.setEbeanServer(internalEbean);
    ebeanServer = internalEbean;
    internalEbean.config().putServiceObject(beanDescriptorManagerProvider);
  }

  @Override
  protected DeployBeanDescriptor<?> targetDescriptor(DeployBeanPropertyAssoc<?> prop) {
    Object tenantId = tenantProvider.currentId();
    if (ebeanServer == null || tenantId == null) {
      return super.targetDescriptor(prop);
    }
    Class<?> targetType = prop.getTargetType();
    BeanDescriptorMapTenant mapTenant = beanDescriptorManagerProvider.getDescriptorTenant(tenantId);
    DeployBeanInfo<?> info = mapTenant.descInfo(targetType);
    if (info == null) {
      throw new PersistenceException("Can not find descriptor [" + targetType + "] for " + prop.getFullBeanName());
    }
    return info.getDescriptor();
  }
}
