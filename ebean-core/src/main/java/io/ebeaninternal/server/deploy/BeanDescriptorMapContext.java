package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;

import java.util.Map;

public class BeanDescriptorMapContext {
  protected final Map<String, BeanTable> beanTableMap;
  protected final Map<String, BeanDescriptor<?>> descMap;

  protected final Map<Class<?>, DeployBeanInfo<?>> descInfoMap;

  public BeanDescriptorMapContext(Map<String, BeanTable> beanTableMap, Map<String, BeanDescriptor<?>> descMap, Map<Class<?>, DeployBeanInfo<?>> descInfoMap) {
    this.beanTableMap = beanTableMap;
    this.descMap = descMap;
    this.descInfoMap = descInfoMap;
  }

  public boolean isDeployed(Class<?> beanClass) {
    return descMap.containsKey(beanClass.getName());
  }

  protected boolean isDeploying(Class<?> entityClass) {
    return beanTableMap.containsKey(entityClass.getName());
  }

  public <T> BeanDescriptor<T> desc(Class<T> beanClass) {
    return (BeanDescriptor<T>) descMap.get(beanClass.getName());
  }

  public BeanTable beanTable(Class<?> type) {
    return beanTableMap.get(type.getName());
  }

  protected DeployBeanInfo<?> descInfo(Class<?> beanClass) {
    return descInfoMap.get(beanClass);
  }
}
