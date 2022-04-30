package io.ebeaninternal.server.core;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.api.SpiBackgroundExecutor;
import io.ebeaninternal.server.cluster.ClusterManager;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.deploy.BeanDescriptorTenantManager;
import io.ebeaninternal.server.deploy.parse.DeployCreateProperties;
import io.ebeaninternal.server.deploy.parse.TenantDeployCreateProperties;
import io.ebeaninternal.server.dto.DtoBeanManager;
import io.ebeaninternal.server.query.CQueryEngine;

import java.util.Map;

public class TenantInternalConfiguration extends InternalConfiguration{
  TenantInternalConfiguration(boolean online, ClusterManager clusterManager, SpiBackgroundExecutor backgroundExecutor, DatabaseConfig config, BootupClasses bootupClasses) {
    super(online, clusterManager, backgroundExecutor, config, bootupClasses);
  }
  @Override
  protected void beanDescriptorManager(){
    deployCreateProperties = new TenantDeployCreateProperties(typeManager);
    final InternalConfigXmlMap xmlMap = initExternalMapping();
    this.dtoBeanManager = new DtoBeanManager(typeManager, xmlMap.readDtoMapping());
    this.beanDescriptorManager = new BeanDescriptorTenantManager(this);
    Map<String, String> asOfTableMapping = beanDescriptorManager.deploy(xmlMap.xmlDeployment());
    Map<String, String> draftTableMap = beanDescriptorManager.draftTableMap();
    beanDescriptorManager.scheduleBackgroundTrim();
    this.dataTimeZone = initDataTimeZone();
    this.binder = getBinder(typeManager, databasePlatform, dataTimeZone);
    this.cQueryEngine = new CQueryEngine(config, databasePlatform, binder, asOfTableMapping, draftTableMap);
  }
}
