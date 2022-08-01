package io.ebeaninternal.server.properties;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.ObjectEntity;
import io.ebeaninternal.server.util.Str;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Returns Getter and Setter methods based on EntityBean enhancement and field position.
 */
public final class BiConsumerPropertyAccess  {
  private List<String> properties = new ArrayList<>();

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
      ObjectEntity entity = (ObjectEntity) bean;
      return entity.getValueIntercept(properties.get(fieldIndex),false);
    }

    @Override
    public Object getIntercept(EntityBean bean) {
      ObjectEntity entity = (ObjectEntity) bean;
      return entity.getValueIntercept(properties.get(fieldIndex),true);
    }
    @Override
    public void set(EntityBean bean, Object value) {
      ObjectEntity entity = (ObjectEntity) bean;
      entity.setValueIntercept(properties.get(fieldIndex),value,false);
    }

    @Override
    public void setIntercept(EntityBean bean, Object value) {
      ObjectEntity entity = (ObjectEntity) bean;
      entity.setValueIntercept(properties.get(fieldIndex),value,true);
    }
  }
}
