package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;

import java.util.Map;

public class BeanDescriptorMapRedeployContext extends BeanDescriptorMapContext {
  protected Class<?> beanClass;

  public BeanDescriptorMapRedeployContext(Class<?> beanClass, Map<String, BeanTable> beanTableMap, Map<String, BeanDescriptor<?>> descMap, Map<Class<?>, DeployBeanInfo<?>> descInfoMap, Map<Class<?>, DeployBeanInfo<?>> rootInfoMap) {
    super(beanTableMap, descMap, descInfoMap, rootInfoMap);
    this.beanClass = beanClass;
  }

  @Override
  protected boolean isDeploying(Class<?> entityClass) {
    if (beanClass.equals(entityClass)) {
      return false;
    }
    return beanTableMap.containsKey(entityClass.getName());
  }

  @Override
  protected Class<?> singleDeployBeanClass() {
    return beanClass;
  }
}
