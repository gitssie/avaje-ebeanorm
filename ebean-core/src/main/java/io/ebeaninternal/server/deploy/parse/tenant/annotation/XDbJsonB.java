package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.annotation.DbJsonB;
import io.ebean.annotation.MutationDetection;
import io.ebean.bean.ToStringBuilder;

import java.lang.annotation.Annotation;

public class XDbJsonB implements DbJsonB {
  private String name = "";
  private MutationDetection mutationDetection = MutationDetection.DEFAULT;
  private int length = 0;

  @Override
  public String name() {
    return name;
  }

  @Override
  public MutationDetection mutationDetection() {
    return mutationDetection;
  }

  @Override
  public int length() {
    return length;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setMutationDetection(MutationDetection mutationDetection) {
    this.mutationDetection = mutationDetection;
  }

  public void setLength(int length) {
    this.length = length;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return DbJsonB.class;
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
