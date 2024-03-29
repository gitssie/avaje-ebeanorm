package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.bean.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.lang.annotation.Annotation;

public class XManyToOne implements ManyToOne {
  private Class<?> targetEntity = void.class;
  private CascadeType[] cascade = new CascadeType[0];
  private FetchType fetch = FetchType.LAZY;

  public XManyToOne() {
  }

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

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.start(this);
    builder.end();
    return builder.toString();
  }
}
