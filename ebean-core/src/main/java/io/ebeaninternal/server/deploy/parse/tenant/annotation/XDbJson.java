package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonType;
import io.ebean.annotation.MutationDetection;

import java.lang.annotation.Annotation;

public class XDbJson implements DbJson {
  private String name = "";
  private MutationDetection mutationDetection = MutationDetection.DEFAULT;
  private int length = 0;
  private DbJsonType storage = DbJsonType.JSON;

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

  @Override
  public DbJsonType storage() {
    return storage;
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

  public void setStorage(DbJsonType storage) {
    this.storage = storage;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return DbJson.class;
  }
}
