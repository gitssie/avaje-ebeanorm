package io.ebeaninternal.server.deploy.parse.tenant;

import java.util.*;

public class XEntity {
  private String label;
  private String name;
  private String tableName;
  private Class<?> beanType;
  private Map<String,XField> fields;

  private boolean custom = false;
  private boolean disabled = false;
  private boolean createable = true;
  private boolean deletable = true;
  private boolean updateable = true;
  private boolean queryable = true;
  private boolean feedEnabled = true;

  public XEntity(Class<?> beanType) {
    this.beanType = beanType;
    this.fields = new HashMap<>();
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public Class<?> getBeanType() {
    return beanType;
  }

  public Collection<XField> getFields() {
    return fields.values();
  }

  public XField addField(XField field) {
    return fields.put(field.getName(),field);
  }

  public XField getField(String name) {
    return fields.get(name);
  }

  public boolean isCustom() {
    return custom;
  }

  public void setCustom(boolean custom) {
    this.custom = custom;
  }

  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  public boolean isCreateable() {
    return createable;
  }

  public void setCreateable(boolean createable) {
    this.createable = createable;
  }

  public boolean isDeletable() {
    return deletable;
  }

  public void setDeletable(boolean deletable) {
    this.deletable = deletable;
  }

  public boolean isUpdateable() {
    return updateable;
  }

  public void setUpdateable(boolean updateable) {
    this.updateable = updateable;
  }

  public boolean isQueryable() {
    return queryable;
  }

  public void setQueryable(boolean queryable) {
    this.queryable = queryable;
  }

  public boolean isFeedEnabled() {
    return feedEnabled;
  }

  public void setFeedEnabled(boolean feedEnabled) {
    this.feedEnabled = feedEnabled;
  }
}
