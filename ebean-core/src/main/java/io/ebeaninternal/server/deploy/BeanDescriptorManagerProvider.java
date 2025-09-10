package io.ebeaninternal.server.deploy;

import io.ebean.DatabaseBuilder;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.EncryptKey;
import io.ebean.config.NamingConvention;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeanservice.docstore.api.DocStoreBeanAdapter;

import java.util.concurrent.ConcurrentHashMap;

public class BeanDescriptorManagerProvider implements BeanDescriptorMap {
  protected final ConcurrentHashMap<Object, BeanDescriptorMapTenant> tenantDescMap = new ConcurrentHashMap<>();
  protected final CurrentTenantProvider tenantProvider;
  protected final BeanDescriptorManagerTenant beanDescriptorManager;

  public BeanDescriptorManagerProvider(BeanDescriptorManagerTenant beanDescriptorManager, CurrentTenantProvider tenantProvider) {
    this.beanDescriptorManager = beanDescriptorManager;
    this.tenantProvider = tenantProvider;
  }

  public BeanDescriptorMapTenant getDescriptorTenant(Object tenantId) {
    BeanDescriptorMapTenant mapTenant = tenantDescMap.get(tenantId);
    if (mapTenant == null) {
      mapTenant = tenantDescMap.computeIfAbsent(tenantId, (key) -> new BeanDescriptorMapTenant(tenantId, beanDescriptorManager));
    }
    return mapTenant;
  }

  public BeanDescriptorMapTenant getDescriptorTenant() {
    return getDescriptorTenant(tenantProvider.currentId());
  }

  public BeanDescriptor<?> createBeanDescriptor(Class<?> beanClass, XEntity entity) throws Exception {
    return getDescriptorTenant().createBeanDescriptor(beanClass, entity);
  }

  public boolean redeploy(Object tenantId, Class<?> entityClass, XEntity entity) {
    return getDescriptorTenant(tenantId).redeploy(entityClass, entity);
  }

  @Override
  public String name() {
    return getClass().getSimpleName();
  }

  @Override
  public DatabaseBuilder.Settings config() {
    return getDescriptorTenant().config();
  }

  @Override
  public SpiCacheManager cacheManager() {
    return getDescriptorTenant().cacheManager();
  }

  @Override
  public NamingConvention namingConvention() {
    return getDescriptorTenant().namingConvention();
  }

  @Override
  public boolean isMultiValueSupported() {
    return getDescriptorTenant().isMultiValueSupported();
  }

  @Override
  public <T> BeanDescriptor<T> descriptor(Class<T> entityType) {
    return getDescriptorTenant().descriptor(entityType);
  }

  @Override
  public EncryptKey encryptKey(String tableName, String columnName) {
    return getDescriptorTenant().encryptKey(tableName, columnName);
  }

  @Override
  public IdBinder createIdBinder(BeanProperty id) {
    return getDescriptorTenant().createIdBinder(id);
  }

  @Override
  public <T> DocStoreBeanAdapter<T> createDocStoreBeanAdapter(BeanDescriptor<T> descriptor, DeployBeanDescriptor<T> deploy) {
    return getDescriptorTenant().createDocStoreBeanAdapter(descriptor, deploy);
  }

  @Override
  public ScalarType<?> scalarType(int jdbcType) {
    return getDescriptorTenant().scalarType(jdbcType);
  }

  @Override
  public ScalarType<?> scalarType(String cast) {
    return getDescriptorTenant().scalarType(cast);
  }

  @Override
  public boolean isJacksonCorePresent() {
    return getDescriptorTenant().isJacksonCorePresent();
  }

  @Override
  public boolean isTableManaged(String tableName) {
    return getDescriptorTenant().isTableManaged(tableName);
  }

  @Override
  public BeanTable beanTable(Class<?> type) {
    return getDescriptorTenant().beanTable(type);
  }
}
