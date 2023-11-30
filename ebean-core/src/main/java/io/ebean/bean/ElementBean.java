package io.ebean.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ElementBean implements EntityBean, Map<String, Object> {

  private static final long serialVersionUID = 8742536671024715601L;

  private String[] properties = new String[0];
  private EntityBeanIntercept intercept;
  private Map<String, Integer> propMap;
  private Map<String, Object> rawData;

  public ElementBean(Map<String, Object> rawData) {
    this.rawData = rawData;
    this.intercept = new InterceptReadWrite(this);
  }

  public ElementBean() {
    this(new HashMap<>());
  }

  @Override
  public String[] _ebean_getPropertyNames() {
    return properties;
  }

  @Override
  public String _ebean_getPropertyName(int pos) {
    return properties[pos];
  }

  @Override
  public Object _ebean_newInstance() {
    return new ElementBean();
  }

  @Override
  public void _ebean_setEmbeddedLoaded() {
  }

  @Override
  public boolean _ebean_isEmbeddedNewOrDirty() {
    return false;
  }

  @Override
  public EntityBeanIntercept _ebean_getIntercept() {
    return intercept;
  }

  @Override
  public EntityBeanIntercept _ebean_intercept() {
    return intercept;
  }

  @Override
  public void _ebean_setField(int fieldIndex, Object value) {
    String key = properties[fieldIndex];
    Object oldVal = rawData.put(key, value);
    this.intercept.preSetter(false, fieldIndex, oldVal, value);
  }

  @Override
  public void _ebean_setFieldIntercept(int fieldIndex, Object value) {
    String key = properties[fieldIndex];
    Object oldVal = rawData.put(key, value);
    this.intercept.preSetter(true, fieldIndex, oldVal, value);
  }

  @Override
  public Object _ebean_getField(int fieldIndex) {
    String name = properties[fieldIndex];
    return rawData.get(name);
  }

  @Override
  public Object _ebean_getFieldIntercept(int fieldIndex) {
    intercept.preGetter(fieldIndex);
    return _ebean_getField(fieldIndex);
  }

  public Object set(String key, Object newVal) {
    Object oldVal = rawData.put(key, newVal);
    if (properties.length > 0 && propMap != null) {
      Integer fieldIndex = propMap.get(key);
      if (fieldIndex != null) {
        this.intercept.preSetter(true, fieldIndex, oldVal, newVal);
      }
    }
    return oldVal;
  }

  public void _ebean_setInterceptProperties(String[] properties, Map<String, Integer> propMap) {
    this.properties = properties;
    this.propMap = propMap;
  }

  public void _ebean_setIntercept(EntityBeanIntercept intercept) {
    this.intercept = intercept;
  }

  @Override
  public int size() {
    return rawData.size();
  }

  @Override
  public boolean isEmpty() {
    return rawData.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return rawData.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return rawData.containsValue(value);
  }

  @Override
  public Object get(Object key) {
    return rawData.get(key);
  }

  @Override
  public Object put(String key, Object value) {
    return set(key, value);
  }

  @Override
  public Object remove(Object key) {
    return rawData.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ?> m) {
    for (Entry<? extends String, ?> entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void clear() {
    rawData.clear();
  }

  @Override
  public Set<String> keySet() {
    return rawData.keySet();
  }

  @Override
  public Collection<Object> values() {
    return rawData.values();
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return rawData.entrySet();
  }

  @Override
  public void toString(ToStringBuilder builder) {
    builder.addMap(rawData);
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder();
    toString(builder);
    return builder.toString();
  }
}
