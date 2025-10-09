package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.annotation.DbComment;

import java.lang.annotation.Annotation;

public class XDbComment implements DbComment {
  private String value = "";

  @Override
  public String value() {
    return value;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return DbComment.class;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
