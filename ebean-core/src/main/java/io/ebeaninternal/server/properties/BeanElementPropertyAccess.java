package io.ebeaninternal.server.properties;

import io.ebean.bean.EntityBean;
import io.ebean.core.type.ScalarType;

import java.util.function.Function;

public class BeanElementPropertyAccess implements BeanPropertyGetter, BeanPropertySetter {
  private final Function<EntityBean, EntityBean> elementBean;
  private final int fieldIndex;
  private final ScalarType<?> scalarType;

  public BeanElementPropertyAccess(Function<EntityBean, EntityBean> elementBean, int fieldIndex, ScalarType<?> scalarType) {
    this.elementBean = elementBean;
    this.fieldIndex = fieldIndex;
    this.scalarType = scalarType;
  }

  @Override
  public Object get(EntityBean bean) {
    Object value = elementBean.apply(bean)._ebean_getField(fieldIndex);
    if (value != null && scalarType != null) {
      value = scalarType.toBeanType(value);
    }
    return value;
  }

  @Override
  public Object getIntercept(EntityBean bean) {
    Object value = elementBean.apply(bean)._ebean_getFieldIntercept(fieldIndex);
    if (value != null && scalarType != null) {
      value = scalarType.toBeanType(value);
    }
    return value;
  }

  @Override
  public void set(EntityBean bean, Object value) {
    elementBean.apply(bean)._ebean_setField(fieldIndex, value);
  }

  @Override
  public void setIntercept(EntityBean bean, Object value) {
    elementBean.apply(bean)._ebean_setFieldIntercept(fieldIndex, value);
  }
}
