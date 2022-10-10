package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.bean.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import java.lang.annotation.Annotation;

public class XOneToOne implements OneToOne {
  private Class<?> targetEntity;
  private CascadeType cascade;
  private FetchType fetch = FetchType.EAGER;
  private String mappedBy;

  public XOneToOne(Class<?> targetEntity, CascadeType cascade,String mappedBy) {
    this.targetEntity = targetEntity;
    this.cascade = cascade;
    this.mappedBy = mappedBy;
  }

  public XOneToOne(Class<?> targetEntity, CascadeType cascade) {
    this(targetEntity,cascade,"");
  }

  @Override
  public Class targetEntity() {
    return targetEntity;
  }

  @Override
  public CascadeType[] cascade() {
    return new CascadeType[]{cascade};
  }

  @Override
  public FetchType fetch() {
    return fetch;
  }

  @Override
  public boolean optional() {
    return true;
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
    return OneToOne.class;
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
