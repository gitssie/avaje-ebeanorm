package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.bean.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.lang.annotation.Annotation;

public class XManyToOne implements ManyToOne {
  private Class<?> targetEntity;
  private CascadeType[] cascade;
  private FetchType fetch = FetchType.EAGER;

  public XManyToOne(Class<?> targetEntity, CascadeType[] cascade) {
    this.targetEntity = targetEntity;
    this.cascade = cascade;
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
    return true;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return ManyToOne.class;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.start(this);
    builder.end();
    return builder.toString();
  }
}
