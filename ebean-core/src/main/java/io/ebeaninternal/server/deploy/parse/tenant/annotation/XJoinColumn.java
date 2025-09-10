package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.bean.ToStringBuilder;

import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import java.lang.annotation.Annotation;

public class XJoinColumn implements JoinColumn {
  private String name = "";
  private String referencedColumnName = "";
  private boolean unique = false;
  private boolean nullable = true;
  private boolean insertable = true;
  private boolean updatable = true;
  private String columnDefinition = "";
  private String table = "";
  private XForeignKey foreignKey = new XForeignKey();

  public XJoinColumn() {
  }

  public XJoinColumn(String name) {
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String referencedColumnName() {
    return referencedColumnName;
  }

  @Override
  public boolean unique() {
    return unique;
  }

  @Override
  public boolean nullable() {
    return nullable;
  }

  @Override
  public boolean insertable() {
    return insertable;
  }

  @Override
  public boolean updatable() {
    return updatable;
  }

  @Override
  public String columnDefinition() {
    return columnDefinition;
  }

  @Override
  public String table() {
    return table;
  }

  @Override
  public ForeignKey foreignKey() {
    return foreignKey;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return JoinColumn.class;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getReferencedColumnName() {
    return referencedColumnName;
  }

  public void setReferencedColumnName(String referencedColumnName) {
    this.referencedColumnName = referencedColumnName;
  }

  public boolean isUnique() {
    return unique;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public boolean isInsertable() {
    return insertable;
  }

  public void setInsertable(boolean insertable) {
    this.insertable = insertable;
  }

  public boolean isUpdatable() {
    return updatable;
  }

  public void setUpdatable(boolean updatable) {
    this.updatable = updatable;
  }

  public String getColumnDefinition() {
    return columnDefinition;
  }

  public void setColumnDefinition(String columnDefinition) {
    this.columnDefinition = columnDefinition;
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public void setForeignKey(XForeignKey foreignKey) {
    this.foreignKey = foreignKey;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder();
    builder.start(this);
    builder.add("name", name);
    builder.add("unique", unique);
    builder.add("nullable", nullable);
    builder.add("insertable", insertable);
    builder.add("updatable", updatable);
    builder.add("table", table);
    builder.add("name", name);
    builder.end();
    return builder.toString();
  }
}
