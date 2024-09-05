package io.ebeaninternal.server.properties;

import io.ebean.bean.ElementBean;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.InterceptReadWrite;

import javax.persistence.PersistenceException;
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

  private void setInterceptValue(ElementBean element, EntityBean owner) {
    if (element == null) {
      throw new PersistenceException("element bean is null");
    }
    if (element._ebean_getPropertyNames() != properties) {
      element._ebean_setInterceptProperties(properties, propMap);
      element._ebean_setIntercept(new BeanElementPropertyIntercept(new InterceptReadWrite(element), owner, elementFieldIndex, slotIndex));
    }
  }

  @Override
  public void set(EntityBean bean, Object value) {
    ElementBean element = (ElementBean) bean._ebean_getField(elementFieldIndex);
    setInterceptValue(element, bean);
    element._ebean_setField(fieldIndex, value);
  }

  @Override
  public void setIntercept(EntityBean bean, Object value) {
    ElementBean element = (ElementBean) bean._ebean_getField(elementFieldIndex);
    setInterceptValue(element, bean);
    element._ebean_setFieldIntercept(fieldIndex, value);
  }
}
