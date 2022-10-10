package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.bean.ToStringBuilder;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.lang.annotation.Annotation;

public class XGeneratedValue implements GeneratedValue {
  private GenerationType strategy = GenerationType.AUTO;
  private String generator = "";

  public XGeneratedValue() {
  }

  public XGeneratedValue(GenerationType strategy, String generator) {
    this.strategy = strategy;
    this.generator = generator;
  }

  public XGeneratedValue(String generator) {
    this.generator = generator;
  }

  @Override
  public GenerationType strategy() {
    return strategy;
  }

  @Override
  public String generator() {
    return generator;
  }

  public void setGenerator(String generator) {
    this.generator = generator;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return GeneratedValue.class;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.start(this);
    builder.add("generator", generator);
    builder.end();
    return builder.toString();
  }
}
