package io.ebean.bean;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ObjectEntity {
  public static final String KEY_CUSTOM = "custom";
  private transient Set<String> dirtyProperties;
  private transient int propertyIndex = -1;

  private void initPropertyIndex(EntityBean bean){
    if(propertyIndex == -1){
      String[] propertyNames = bean._ebean_getPropertyNames();
      for(int i=0;i<propertyNames.length;i++){
        if(propertyNames[i] == KEY_CUSTOM){
          propertyIndex = i + 1;
          break;
        }
      }
      propertyIndex --;
    }
  }

  public void set(String key, Object value) {
    setValueIntercept(key,value,true);
  }

  public void setValueIntercept(String key, Object value,boolean isIntercept){
    EntityBean bean = ((EntityBean) this);
    EntityBeanIntercept intercept = bean._ebean_getIntercept();
    initPropertyIndex(bean);
    Map<String,Object> custom = this.custom();
    /*boolean isNew = intercept.isNew();
    if(isNew){
      intercept.setLoadedProperty(propertyIndex);
    }else */if(InterceptReadWrite.notEqual(value,custom.get(key))){
      if(dirtyProperties == null){
        dirtyProperties = new HashSet<>();
      }
      intercept.preSetter(isIntercept,propertyIndex,custom.get(key),value);
      dirtyProperties.add(key);
    }
    custom.put(key,value);
  }

  public Object get(String key) {
    return custom().get(key);
  }

  public boolean has(String key){
    Map<String,Object> cust = custom();
    return cust != null && cust.containsKey(key);
  }

  public abstract Map<String,Object> custom();

  public Set<String> dirtySet() {
    return dirtyProperties;
  }
}
