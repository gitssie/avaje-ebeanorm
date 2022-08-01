package io.ebean.bean;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ObjectEntity {
  public static final String KEY_CUSTOM = "custom";
  private transient Set<String> dirtyProperties;
  private transient int propertyIndex = -1;

  private void initPropertyIndex(EntityBean bean) {
    if (propertyIndex == -1) {
      String[] propertyNames = bean._ebean_getPropertyNames();
      for (int i = 0; i < propertyNames.length; i++) {
        if (propertyNames[i].equals(KEY_CUSTOM)) {
          propertyIndex = i + 1;
          break;
        }
      }
      propertyIndex--;
    }
  }

  public void put(String key, Object value) {
    set(key, value);
  }

  public void set(String key, Object value) {
    setValueIntercept(key, value, true);
  }

  public void setValueIntercept(String key, Object value, boolean isIntercept) {
    EntityBean bean = ((EntityBean) this);
    EntityBeanIntercept intercept = bean._ebean_getIntercept();
    initPropertyIndex(bean);
    Map<String, Object> custom = this.custom();
    Object oldValue = custom.get(key);
    if (isIntercept && InterceptReadWrite.notEqual(oldValue, value)) {
      if (dirtyProperties == null) {
        dirtyProperties = new HashSet<>();
      }
      dirtyProperties.add(key);
    }
    intercept.preSetter(isIntercept, propertyIndex, oldValue, value);
    custom.put(key, value);
  }

  public Object get(String key) {
    return getValueIntercept(key,true);
  }

  public Object getValueIntercept(String key, boolean isIntercept) {
    if(isIntercept) {
      EntityBean bean = ((EntityBean) this);
      EntityBeanIntercept intercept = bean._ebean_getIntercept();
      initPropertyIndex(bean);
      intercept.preGetter(propertyIndex);
    }
    Map<String, Object> custom = this.custom();
    return custom.get(key);
  }

  public boolean has(String key) {
    Object value = getValueIntercept(key,true);
    return value != null;
  }

  public abstract Map<String, Object> custom();

  public Set<String> dirtySet() {
    return dirtyProperties;
  }
}
