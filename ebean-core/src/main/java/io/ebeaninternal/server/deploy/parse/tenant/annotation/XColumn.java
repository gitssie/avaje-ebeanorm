package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.bean.ToStringBuilder;

import javax.persistence.Column;
import java.lang.annotation.Annotation;

public class XColumn implements Column {
  private String name = "";
  private String columnDefinition = "";
  private String table = "";
  private boolean unique = false;
  private boolean nullable = true;
  private boolean insertable = true;
  private boolean updatable = true;
  private int length = 255;
  private int precision = 0;
  private int scale = 0;


  public XColumn() {
  }

  public XColumn(String name) {
    this.name = name;
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
  public int length() {
    return length;
  }

  @Override
  public int precision() {
    return precision;
  }

  @Override
  public int scale() {
    return scale;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return Column.class;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

  public int getScale() {
    return scale;
  }

  public void setScale(int scale) {
    this.scale = scale;
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
