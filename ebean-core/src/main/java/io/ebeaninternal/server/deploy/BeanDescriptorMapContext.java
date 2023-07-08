package io.ebeaninternal.server.deploy;

import io.ebean.Model;
import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;

import java.util.Map;
import java.util.function.Function;

public class BeanDescriptorMapContext {
  protected final Map<String, BeanTable> beanTableMap;
  protected final Map<String, BeanDescriptor<?>> descMap;

  protected final Map<Class<?>, DeployBeanInfo<?>> descInfoMap;

  protected final Map<Class<?>, DeployBeanInfo<?>> rootInfoMap;


  public BeanDescriptorMapContext(Map<String, BeanTable> beanTableMap, Map<String, BeanDescriptor<?>> descMap, Map<Class<?>, DeployBeanInfo<?>> descInfoMap, Map<Class<?>, DeployBeanInfo<?>> rootInfoMap) {
    this.beanTableMap = beanTableMap;
    this.descMap = descMap;
    this.descInfoMap = descInfoMap;
    this.rootInfoMap = rootInfoMap;
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

  public DeployBeanInfo<?> getDescInfo(Class<?> beanClass, Function<Class<?>, DeployBeanInfo<?>> temporal) {
    DeployBeanInfo<?> info = descInfoMap.get(beanClass);
    if (info == null) {
      info = temporal.apply(beanClass);
    }
    if (info == null) {
      info = rootInfoMap.get(beanClass);
    }
    return info;
  }

  public DeployBeanInfo<?> getRootDescInfo(Class<?> beanClass) {
    DeployBeanInfo<?> info = rootInfoMap.get(beanClass); //这里是从父级集成来的
    Class<?> clazz = beanClass;
    while (info == null && !(clazz.equals(Object.class) || clazz.equals(Model.class))) {
      clazz = clazz.getSuperclass();
      info = rootInfoMap.get(clazz);
    }
    return info;
  }
}
