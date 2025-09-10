package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.bean.ToStringBuilder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import java.lang.annotation.Annotation;

public class XOneToOne implements OneToOne {
  private Class<?> targetEntity = void.class;
  private CascadeType[] cascade = new CascadeType[0];
  private FetchType fetch = FetchType.LAZY;
  private String mappedBy = "";
  private boolean optional = true;
  private boolean orphanRemoval = false;
  public XOneToOne() {
  }

  public XOneToOne(Class<?> targetEntity, CascadeType[] cascade, String mappedBy) {
    this.targetEntity = targetEntity;
    this.cascade = cascade;
    this.mappedBy = mappedBy;
  }

  public XOneToOne(Class<?> targetEntity, CascadeType[] cascade) {
    this(targetEntity, cascade, "");
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
  public boolean optional() {
    return optional;
  }

  @Override
  public String mappedBy() {
    return mappedBy == null ? "" : mappedBy;
  }

  @Override
  public boolean orphanRemoval() {
    return orphanRemoval;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return OneToOne.class;
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
