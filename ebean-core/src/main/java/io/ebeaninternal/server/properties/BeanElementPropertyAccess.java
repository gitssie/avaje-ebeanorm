package io.ebeaninternal.server.properties;

import io.ebean.bean.EntityBean;

import java.util.function.Function;

public class BeanElementPropertyAccess implements BeanPropertyGetter, BeanPropertySetter {
  private final Function<EntityBean, EntityBean> elementBean;
  private final int fieldIndex;

  public BeanElementPropertyAccess(Function<EntityBean, EntityBean> elementBean, int fieldIndex) {
    this.elementBean = elementBean;
    this.fieldIndex = fieldIndex;
  }

  @Override
  public Object get(EntityBean bean) {
    return elementBean.apply(bean)._ebean_getField(fieldIndex);
  }

  @Override
  public Object getIntercept(EntityBean bean) {
    return elementBean.apply(bean)._ebean_getFieldIntercept(fieldIndex);
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
