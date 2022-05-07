package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import javax.persistence.Column;
import java.lang.annotation.Annotation;

public class XColumn implements Column {
  private String name;

  public XColumn(String name) {
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean unique() {
    return false;
  }

  @Override
  public boolean nullable() {
    return false;
  }

  @Override
  public boolean insertable() {
    return false;
  }

  @Override
  public boolean updatable() {
    return false;
  }

  @Override
  public String columnDefinition() {
    return null;
  }

  @Override
  public String table() {
    return null;
  }

  @Override
  public int length() {
    return 0;
  }

  @Override
  public int precision() {
    return 0;
  }

  @Override
  public int scale() {
    return 0;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return Column.class;
  }
}
