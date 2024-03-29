package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.bean.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.lang.annotation.Annotation;

public class XOneToMany implements OneToMany {
  private Class<?> targetEntity = void.class;
  private CascadeType[] cascade = new CascadeType[0];
  private FetchType fetch = FetchType.LAZY;
  private String mappedBy = "";

  public XOneToMany() {
  }

  public XOneToMany(Class<?> targetEntity, CascadeType[] cascade, FetchType fetch, String mappedBy) {
    this.targetEntity = targetEntity;
    this.cascade = cascade;
    this.fetch = fetch;
    this.mappedBy = mappedBy;
  }

  public XOneToMany(Class<?> targetEntity, CascadeType[] cascade, FetchType fetch) {
      this(targetEntity,cascade,fetch,null);
  }

  @Override
  public Class targetEntity() {
    return targetEntity;
  }

  @Override
  public CascadeType[] cascade() {
    return cascade;
  }

  @Override
  public FetchType fetch() {
    return fetch;
  }

  @Override
  public String mappedBy() {
    return mappedBy == null ? "" : mappedBy;
  }

  @Override
  public boolean orphanRemoval() {
    return false;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return OneToMany.class;
  }

  public Class<?> getTargetEntity() {
    return targetEntity;
  }

  public void setTargetEntity(Class<?> targetEntity) {
    this.targetEntity = targetEntity;
  }

  public CascadeType[] getCascade() {
    return cascade;
  }

  public void setCascade(CascadeType[] cascade) {
    this.cascade = cascade;
  }

  public FetchType getFetch() {
    return fetch;
  }

  public void setFetch(FetchType fetch) {
    this.fetch = fetch;
  }

  public String getMappedBy() {
    return mappedBy;
  }

  public void setMappedBy(String mappedBy) {
    this.mappedBy = mappedBy;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.start(this);
    builder.add("mappedBy", mappedBy);
    builder.end();
    return builder.toString();
  }
}
