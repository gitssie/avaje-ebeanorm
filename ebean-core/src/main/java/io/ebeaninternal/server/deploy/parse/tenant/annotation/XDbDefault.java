package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.annotation.DbDefault;

import java.lang.annotation.Annotation;

public class XDbDefault implements DbDefault {
  private String value;

  public XDbDefault(String value) {
    this.value = value;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return DbDefault.class;
  }
}
