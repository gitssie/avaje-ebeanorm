package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.bean.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.lang.annotation.Annotation;

public class XOneToMany implements OneToMany {
  private Class<?> targetEntity;
  private CascadeType[] cascade;
  private FetchType fetch = FetchType.EAGER;
  private String mappedBy;

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

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.start(this);
    builder.add("mappedBy", mappedBy);
    builder.end();
    return builder.toString();
  }
}
