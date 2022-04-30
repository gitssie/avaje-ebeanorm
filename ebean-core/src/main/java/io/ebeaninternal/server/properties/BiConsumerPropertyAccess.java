package io.ebeaninternal.server.properties;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.server.util.Str;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Returns Getter and Setter methods based on EntityBean enhancement and field position.
 */
public final class BiConsumerPropertyAccess  {
  private int startAt;
  private List<String> properties = new ArrayList<>();

  public BiConsumerPropertyAccess(int startAt){
    this.startAt = startAt;
  }

  public int addProperties(String propertyName){
    int len = properties.size();
    properties.add(propertyName);
    return len;
  }

  public GetterSetter getGetter(int position) {
    return new GetterSetter(position);
  }

  private final class GetterSetter implements BeanPropertyGetter,BeanPropertySetter {

    private final int fieldIndex;

    GetterSetter(int fieldIndex) {
      this.fieldIndex = fieldIndex;
    }

    @Override
    public Object get(EntityBean bean) {
      Function<String,Object> function = (Function<String, Object>) bean;
      return function.apply(properties.get(fieldIndex));
    }

    @Override
    public Object getIntercept(EntityBean bean) {
      Function<String,Object> function = (Function<String, Object>) bean;
      return function.apply(properties.get(fieldIndex));
    }
    @Override
    public void set(EntityBean bean, Object value) {
      bean._ebean_getIntercept();
      BiConsumer<String,Object> consumer = (BiConsumer) bean;
      consumer.accept(properties.get(fieldIndex),value);
    }

    @Override
    public void setIntercept(EntityBean bean, Object value) {
      BiConsumer<String,Object> consumer = (BiConsumer) bean;
      consumer.accept(properties.get(fieldIndex),value);
    }
  }
}
