package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.annotation.Index;
import io.ebean.annotation.Platform;
import io.ebean.bean.ToStringBuilder;

import java.lang.annotation.Annotation;

public class XIndex implements Index {

  private String name = "";
  private boolean unique = false;
  private boolean concurrent = false;
  private String definition = "";
  private String[] columnNames = new String[0];
  private Platform[] platforms = new Platform[0];

  public XIndex() {
  }

  public XIndex(String name, boolean unique) {
    this.name = name;
    this.unique = unique;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean unique() {
    return unique;
  }

  @Override
  public boolean concurrent() {
    return concurrent;
  }

  @Override
  public String[] columnNames() {
    return columnNames;
  }

  @Override
  public Platform[] platforms() {
    return platforms;
  }

  @Override
  public String definition() {
    return definition == null ? "" : definition;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return Index.class;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public void setConcurrent(boolean concurrent) {
    this.concurrent = concurrent;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public void setColumnNames(String[] columnNames) {
    this.columnNames = columnNames;
  }

  public void setPlatforms(Platform[] platforms) {
    this.platforms = platforms;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.start(this);
    builder.add("name", name);
    builder.add("unique", unique);
    builder.add("concurrent", concurrent);
    builder.add("definition", definition);
    builder.add("columnNames", columnNames);
    builder.end();
    return builder.toString();
  }
}
