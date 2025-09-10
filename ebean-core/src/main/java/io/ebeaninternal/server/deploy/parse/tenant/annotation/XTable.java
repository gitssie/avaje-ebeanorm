package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.bean.ToStringBuilder;

import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.lang.annotation.Annotation;

public class XTable implements Table {
  private String name = "";
  private String catalog = "";
  private String schema = "";

  public XTable(String name) {
    this(name, "", "");
  }

  public XTable(String name, String catalog, String schema) {
    this.name = name;
    this.catalog = catalog;
    this.schema = schema;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String catalog() {
    return catalog;
  }

  @Override
  public String schema() {
    return schema;
  }

  @Override
  public UniqueConstraint[] uniqueConstraints() {
    return new UniqueConstraint[0];
  }

  @Override
  public Index[] indexes() {
    return new Index[0];
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return Table.class;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.start(this);
    builder.add("name", name);
    builder.add("catalog", catalog);
    builder.add("schema", schema);
    builder.end();
    return builder.toString();
  }
}
