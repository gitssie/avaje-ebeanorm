package io.ebean.bean;

import io.ebeaninternal.server.core.BasicTypeConverter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public interface DynamicEntity {
  void put(String key, Object value);

  void set(String key, Object value);

  Object get(String key);

  boolean has(String key);

  default UUID getUUID(String name) {
    return BasicTypeConverter.toUUID(get(name), true);
  }

  default Boolean getBoolean(String name) {
    return BasicTypeConverter.toBoolean(get(name), "true");
  }

  default boolean getBooleanValue(String name) {
    return getBooleanValue(name, false);
  }

  default boolean getBooleanValue(String name, boolean val) {
    Boolean booleanVal = getBoolean(name);
    return booleanVal == null ? val : booleanVal.booleanValue();
  }

  default Integer getInteger(String name) {
    return BasicTypeConverter.toInteger(get(name));
  }

  default int getIntegerValue(String name) {
    return getIntegerValue(name, 0);
  }

  default int getIntegerValue(String name, int val) {
    Integer intVal = BasicTypeConverter.toInteger(get(name));
    return intVal == null ? val : intVal.intValue();
  }

  default BigDecimal getBigDecimal(String name) {
    return BasicTypeConverter.toBigDecimal(get(name));
  }

  default BigDecimal getBigDecimalValue(String name) {
    return getBigDecimalValue(name, BigDecimal.ZERO);
  }

  default BigDecimal getBigDecimalValue(String name, BigDecimal val) {
    BigDecimal bigVal = BasicTypeConverter.toBigDecimal(get(name));
    return bigVal == null ? val : bigVal;
  }

  default BigInteger getBigInteger(String name) {
    return BasicTypeConverter.toMathBigInteger(get(name));
  }

  default BigInteger getBigIntegerValue(String name) {
    return getBigIntegerValue(name, BigInteger.ZERO);
  }

  default BigInteger getBigIntegerValue(String name, BigInteger val) {
    BigInteger bigVal = BasicTypeConverter.toMathBigInteger(get(name));
    return bigVal == null ? val : bigVal;
  }

  default Long getLong(String name) {
    return BasicTypeConverter.toLong(get(name));
  }

  default long getLongValue(String name) {
    return getLongValue(name, 0);
  }

  default long getLongValue(String name, long val) {
    Long longVal = BasicTypeConverter.toLong(get(name));
    return longVal == null ? val : longVal.longValue();
  }

  default Double getDouble(String name) {
    return BasicTypeConverter.toDouble(get(name));
  }

  default double getDoubleValue(String name) {
    return getDoubleValue(name, 0);
  }

  default double getDoubleValue(String name, double val) {
    Double dVal = BasicTypeConverter.toDouble(get(name));
    return dVal == null ? val : dVal.doubleValue();
  }

  default Float getFloat(String name) {
    return BasicTypeConverter.toFloat(get(name));
  }

  default float getFloatValue(String name) {
    return getFloatValue(name, 0);
  }

  default float getFloatValue(String name, float val) {
    Float fVal = BasicTypeConverter.toFloat(get(name));
    return fVal == null ? val : fVal.floatValue();
  }

  default String getString(String name) {
    return BasicTypeConverter.toString(get(name));
  }

  default String getString(String name, String val) {
    String strVal = BasicTypeConverter.toString(get(name));
    return strVal == null ? val : strVal;
  }

  default Date getDate(String name) {
    return BasicTypeConverter.toUtilDate(get(name));
  }

  default Date getDate(String name, Date val) {
    Date dateVal = BasicTypeConverter.toUtilDate(get(name));
    return dateVal == null ? val : dateVal;
  }

  default Timestamp getTimestamp(String name) {
    return BasicTypeConverter.toTimestamp(get(name));
  }

  default Timestamp getTimestamp(String name, Timestamp val) {
    Timestamp tVal = BasicTypeConverter.toTimestamp(get(name));
    return tVal == null ? val : tVal;
  }
}
