package io.ebeaninternal.server.deploy;

import io.ebean.config.DatabaseConfig;
import io.ebean.config.EncryptKey;
import io.ebean.config.NamingConvention;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.cache.SpiCacheManager;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeanservice.docstore.api.DocStoreBeanAdapter;

import java.util.function.Consumer;

public class BeanDescriptorMapTenantProxy implements BeanDescriptorMap {
  private final BeanDescriptorMap proxy;
  private transient BeanDescriptorMapTemporal temporal;

  public BeanDescriptorMapTenantProxy(BeanDescriptorMap proxy, BeanDescriptorMapTemporal temporal) {
    this.proxy = proxy;
    this.temporal = temporal;
  }

  @Override
  public String name() {
    return proxy.name();
  }

  @Override
  public DatabaseConfig.Settings config() {
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

  @Override
  public <T> BeanDescriptor<T> descriptor(Class<T> entityType) {
    if (temporal != null) {
      return temporal.descriptor(entityType);
    }
    return proxy.descriptor(entityType);
  }

  @Override
  public BeanTable beanTable(Class<?> type) {
    if (temporal != null) {
      return temporal.beanTable(type);
    }
    return proxy.beanTable(type);
  }

  @Override
  public <T> void listenDescriptor(Class<?> entityType, Class<T> targetClass, Consumer<BeanDescriptor<T>> consumer) {
    if (temporal != null) {
      temporal.listenDescriptor(entityType, targetClass, consumer);
    } else {
      proxy.listenDescriptor(entityType, targetClass, consumer);
    }
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

  protected void clear() {
    temporal = null;
  }
}
