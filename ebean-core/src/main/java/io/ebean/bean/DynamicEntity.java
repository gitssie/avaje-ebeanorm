package io.ebean.bean;

public interface DynamicEntity {
  void put(String key, Object value);

  void set(String key, Object value);

  Object get(String key);

  boolean has(String key);
}
