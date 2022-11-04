package io.ebean.bean;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ObjectEntity implements EntityBean {
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
    EntityBeanIntercept intercept = this._ebean_getIntercept();
    initPropertyIndex(this);
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
    return getValueIntercept(key, true);
  }

  public Object getValueIntercept(String key, boolean isIntercept) {
    if (isIntercept) {
      EntityBeanIntercept intercept = this._ebean_getIntercept();
      initPropertyIndex(this);
      intercept.preGetter(propertyIndex);
    }
    Map<String, Object> custom = this.custom();
    return custom.get(key);
  }

  public boolean has(String key) {
    Object value = getValueIntercept(key, true);
    return value != null;
  }

  public abstract Map<String, Object> custom();

  public Set<String> dirtySet() {
    return dirtyProperties;
  }
}
