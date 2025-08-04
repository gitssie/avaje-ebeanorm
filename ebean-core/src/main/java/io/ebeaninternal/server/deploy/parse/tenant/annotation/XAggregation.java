package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.annotation.Aggregation;

import java.lang.annotation.Annotation;

public class XAggregation implements Aggregation {
  private String value = "";

  public XAggregation() {
  }

  public XAggregation(String value) {
    this.value = value;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return Aggregation.class;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
