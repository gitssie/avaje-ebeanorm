package io.ebeaninternal.server.properties;

import io.ebean.bean.EntityBean;

import java.util.function.Function;

public class BeanPropertyConvertGetter implements BeanPropertyGetter {
  private Function<Object, Object> function;

  public BeanPropertyConvertGetter(Function<Object, Object> function) {
    this.function = function;
  }

  @Override
  public Object get(EntityBean bean) {
    return function.apply(bean);
  }

  @Override
  public Object getIntercept(EntityBean bean) {
    return function.apply(bean);
  }
}
