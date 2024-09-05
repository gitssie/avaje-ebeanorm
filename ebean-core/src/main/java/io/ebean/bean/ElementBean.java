package io.ebean.bean;

import io.ebeaninternal.server.core.BasicTypeConverter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

public final class ElementBean implements EntityBean, Map<String, Object> {
  private static final long serialVersionUID = 8742536671024715601L;
  private static final String[] EMPTY_PROP = new String[0];
  private String[] properties = EMPTY_PROP;
  private EntityBeanIntercept intercept;
  private Map<String, Integer> propMap;
  private Map<String, Object> rawData;

  public ElementBean(Map<String, Object> rawData) {
    this.rawData = rawData;
    this.propMap = Collections.EMPTY_MAP;
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
    this.intercept.preSetter(false, fieldIndex, rawData.put(properties[fieldIndex], value), value);
  }

  @Override
  public void _ebean_setFieldIntercept(int fieldIndex, Object value) {
    this.intercept.preGetter(fieldIndex);
    this.intercept.preSetter(true, fieldIndex, rawData.put(properties[fieldIndex], value), value);
  }

  @Override
  public Object _ebean_getField(int fieldIndex) {
    return rawData.get(properties[fieldIndex]);
  }

  @Override
  public Object _ebean_getFieldIntercept(int fieldIndex) {
    this.intercept.preGetter(fieldIndex);
    return _ebean_getField(fieldIndex);
  }

  @Override
  public Object get(Object key) {
    Integer fieldIndex = propMap.get(key);
    if (fieldIndex != null) {
      this.intercept.preGetter(fieldIndex);
    }
    return rawData.get(key);
  }

  @Override
  public Object put(String key, Object value) {
    return set(key, value);
  }


  public Object set(String key, Object newVal) {
    Integer fieldIndex = propMap.get(key);
    if (fieldIndex != null) {
      this.intercept.preGetter(fieldIndex);
      Object oldVal = rawData.put(key, newVal);
      this.intercept.preSetter(true, fieldIndex, oldVal, newVal);
      return oldVal;
    }
    return rawData.put(key, newVal);
  }

  public void _ebean_setInterceptProperties(String[] properties, Map<String, Integer> propMap) {
    this.properties = properties;
    this.propMap = propMap;
  }

  public void _ebean_setIntercept(EntityBeanIntercept intercept) {
    for (String key : rawData.keySet()) {
      Integer fieldIndex = propMap.get(key);
      if (fieldIndex != null) {
        intercept.setLoadedProperty(fieldIndex);
      }
    }
    if (this.intercept.isLoaded()) {
      intercept.setLoadedLazy();
    }
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


  public UUID getUUID(String name) {
    return BasicTypeConverter.toUUID(get(name), true);
  }

  public Boolean getBoolean(String name) {
    return BasicTypeConverter.toBoolean(get(name), "true");
  }

  public boolean getBooleanValue(String name) {
    return getBooleanValue(name, false);
  }

  public boolean getBooleanValue(String name, boolean val) {
    Boolean booleanVal = getBoolean(name);
    return booleanVal == null ? val : booleanVal.booleanValue();
  }

  public Integer getInteger(String name) {
    return BasicTypeConverter.toInteger(get(name));
  }

  public int getIntegerValue(String name) {
    return getIntegerValue(name, 0);
  }

  public int getIntegerValue(String name, int val) {
    Integer intVal = BasicTypeConverter.toInteger(get(name));
    return intVal == null ? val : intVal.intValue();
  }

  public BigDecimal getBigDecimal(String name) {
    return BasicTypeConverter.toBigDecimal(get(name));
  }

  public BigDecimal getBigDecimalValue(String name) {
    return getBigDecimalValue(name, BigDecimal.ZERO);
  }

  public BigDecimal getBigDecimalValue(String name, BigDecimal val) {
    BigDecimal bigVal = BasicTypeConverter.toBigDecimal(get(name));
    return bigVal == null ? val : bigVal;
  }

  public BigInteger getBigInteger(String name) {
    return BasicTypeConverter.toMathBigInteger(get(name));
  }

  public BigInteger getBigIntegerValue(String name) {
    return getBigIntegerValue(name, BigInteger.ZERO);
  }

  public BigInteger getBigIntegerValue(String name, BigInteger val) {
    BigInteger bigVal = BasicTypeConverter.toMathBigInteger(get(name));
    return bigVal == null ? val : bigVal;
  }

  public Long getLong(String name) {
    return BasicTypeConverter.toLong(get(name));
  }

  public long getLongValue(String name) {
    return getLongValue(name, 0);
  }

  public long getLongValue(String name, long val) {
    Long longVal = BasicTypeConverter.toLong(get(name));
    return longVal == null ? val : longVal.longValue();
  }

  public Double getDouble(String name) {
    return BasicTypeConverter.toDouble(get(name));
  }

  public double getDoubleValue(String name) {
    return getDoubleValue(name, 0);
  }

  public double getDoubleValue(String name, double val) {
    Double dVal = BasicTypeConverter.toDouble(get(name));
    return dVal == null ? val : dVal.doubleValue();
  }

  public Float getFloat(String name) {
    return BasicTypeConverter.toFloat(get(name));
  }

  public float getFloatValue(String name) {
    return getFloatValue(name, 0);
  }

  public float getFloatValue(String name, float val) {
    Float fVal = BasicTypeConverter.toFloat(get(name));
    return fVal == null ? val : fVal.floatValue();
  }

  public String getString(String name) {
    return BasicTypeConverter.toString(get(name));
  }

  public String getString(String name, String val) {
    String strVal = BasicTypeConverter.toString(get(name));
    return strVal == null ? val : strVal;
  }

  public Date getDate(String name) {
    return BasicTypeConverter.toUtilDate(get(name));
  }

  public Date getDate(String name, Date val) {
    Date dateVal = BasicTypeConverter.toUtilDate(get(name));
    return dateVal == null ? val : dateVal;
  }

  public Timestamp getTimestamp(String name) {
    return BasicTypeConverter.toTimestamp(get(name));
  }

  public Timestamp getTimestamp(String name, Timestamp val) {
    Timestamp tVal = BasicTypeConverter.toTimestamp(get(name));
    return tVal == null ? val : tVal;
  }
}
