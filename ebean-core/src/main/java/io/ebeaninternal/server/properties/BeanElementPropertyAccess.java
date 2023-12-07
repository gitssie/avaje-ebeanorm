package io.ebeaninternal.server.properties;

import io.ebean.bean.ElementBean;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.InterceptReadWrite;

import java.util.Map;

public class BeanElementPropertyAccess implements BeanPropertyGetter, BeanPropertySetter {
  private final int elementFieldIndex;
  private final int slotIndex;
  private final String[] properties;
  private final Map<String, Integer> propMap;
  private final int fieldIndex;

  public BeanElementPropertyAccess(int elementFieldIndex, int slotIndex, String[] properties, Map<String, Integer> propMap, int fieldIndex) {
    this.elementFieldIndex = elementFieldIndex;
    this.slotIndex = slotIndex;
    this.properties = properties;
    this.propMap = propMap;
    this.fieldIndex = fieldIndex;
  }

  @Override
  public Object get(EntityBean bean) {
    ElementBean element = (ElementBean) bean._ebean_getField(elementFieldIndex);
    if (element == null) {
      return null;
    }
    setInterceptValue(element, bean);
    return element._ebean_getField(fieldIndex);
  }

  @Override
  public Object getIntercept(EntityBean bean) {
    ElementBean element = (ElementBean) bean._ebean_getFieldIntercept(elementFieldIndex);
    if (element == null) {
      return null;
    }
    setInterceptValue(element, bean);
    return element._ebean_getFieldIntercept(fieldIndex);
  }

  private EntityBeanIntercept setInterceptValue(ElementBean element, EntityBean owner) {
    if (element == null) {
      throw new RuntimeException("element bean is null");
    }
    if (element._ebean_getPropertyNames().length != properties.length) {
      EntityBeanIntercept ownerI = owner._ebean_getIntercept();
      element._ebean_setInterceptProperties(properties, propMap);
      EntityBeanIntercept intercept = new BeanElementPropertyIntercept(new InterceptReadWrite(element));
      element._ebean_setIntercept(intercept);
      ownerI.setLoadedProperty(elementFieldIndex);
      intercept.setEmbeddedOwner(owner, elementFieldIndex);
      return intercept;
    }
    return element._ebean_getIntercept();
  }

  @Override
  public void set(EntityBean bean, Object value) {
    ElementBean element = (ElementBean) bean._ebean_getField(elementFieldIndex);
    EntityBeanIntercept ebi = setInterceptValue(element, bean);
    if (!ebi.isDirtyProperty(fieldIndex)) {
      element._ebean_setField(fieldIndex, value);
    }
  }

  @Override
  public void setIntercept(EntityBean bean, Object value) {
    ElementBean element = (ElementBean) bean._ebean_getField(elementFieldIndex);
    setInterceptValue(element, bean);
    element._ebean_setFieldIntercept(fieldIndex, value);
  }
}
