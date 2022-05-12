package io.ebeaninternal.server.deploy.parse.tenant;

import java.lang.annotation.Annotation;
import java.util.*;

public class XEntity {
  private String label;
  private String name;
  private Class<?> beanType;
  private Map<String,XField> fields;
  private Map<Class<? extends Annotation>,Annotation> annotations;
  private boolean custom = false;
  private boolean disabled = false;
  private boolean createable = true;
  private boolean deletable = true;
  private boolean updateable = true;
  private boolean queryable = true;
  private boolean feedEnabled = true;

  private String etag; //缓存使用的ETAG

  public XEntity(Class<?> beanType) {
    this.beanType = beanType;
    this.fields = new HashMap<>();
    this.annotations = new HashMap<>();
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

  public Collection<Annotation> getAnnotations() {
    return annotations.values();
  }

  public <T extends Annotation> boolean has(Class<T> annotation){
    return annotations.containsKey(annotation);
  }

  public void addAnnotation(Annotation annotation) {
    this.annotations.put(annotation.annotationType(),annotation);
  }

  public <T extends Annotation> T getAnnotation(Class<T> annClass) {
    return (T) annotations.get(annClass);
  }

  public String getEtag() {
    return etag;
  }

  public void setEtag(String etag) {
    this.etag = etag;
  }
}
