package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.annotation.DbMap;
import io.ebean.bean.ToStringBuilder;

import javax.persistence.Lob;
import java.lang.annotation.Annotation;

public class XDbMap implements DbMap {
  @Lob
  private String name = "";
  private int length;

  @Override
  public String name() {
    return name;
  }

  @Override
  public int length() {
    return length;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setLength(int length) {
    this.length = length;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return DbMap.class;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.start(this);
    builder.add("name", name);
    builder.end();
    return builder.toString();
  }
}
