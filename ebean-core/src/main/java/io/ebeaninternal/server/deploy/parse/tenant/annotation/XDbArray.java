package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.annotation.DbArray;

import java.lang.annotation.Annotation;

public class XDbArray implements DbArray {
  private String name = "";
  private int length = 0;
  private boolean nullable = false;

  @Override
  public String name() {
    return name;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public boolean nullable() {
    return nullable;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return DbArray.class;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }
}
