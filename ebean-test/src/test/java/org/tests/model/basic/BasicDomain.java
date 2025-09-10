package org.tests.model.basic;

import io.ebean.annotation.DbJson;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import io.ebean.bean.DynamicEntity;
import io.ebean.bean.ElementBean;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@MappedSuperclass
public class BasicDomain implements DynamicEntity,Serializable {

  private static final long serialVersionUID = 5569496199004449769L;

  @Id
  Integer id;

  @WhenCreated
  Timestamp cretime;

  @WhenModified
  Timestamp updtime;

  @Version
  Long version;

  @Embedded
  @DbJson
  private ElementBean custom = new ElementBean();
  @Transient
  private final int __slot__ = 0;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Timestamp getUpdtime() {
    return updtime;
  }

  public void setUpdtime(Timestamp updtime) {
    this.updtime = updtime;
  }

  public Timestamp getCretime() {
    return cretime;
  }

  public void setCretime(Timestamp cretime) {
    this.cretime = cretime;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public void put(String key, Object value) {
    custom.put(key, value);
  }

  public void set(String key, Object value) {
    custom.put(key, value);
  }

  public Object get(String key) {
    return custom.get(key);
  }

  public boolean has(String key) {
    return custom.containsKey(key);
  }
}
