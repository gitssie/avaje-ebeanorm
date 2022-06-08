package io.ebeaninternal.server.deploy.parse.tenant;

import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import java.lang.annotation.Annotation;
import java.util.*;

public class XField {
  private String label;
  private String name;
  private Class<?> type;
  private boolean enabled = true;
  private boolean nullable = true;
  private boolean required = false;
  private boolean createable = true;
  private boolean updateable = true;
  private boolean sortable = true;
  private Integer minLength = null;
  private Integer maxLength = null;
  private Map<Class<? extends Annotation>, Annotation> annotations;

  private String etag;

  public XField(String name) {
    this(name, null);
  }

  public XField(String name, Class<?> type) {
    this.name = name;
    this.type = type;
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

  public Class<?> getType() {
    return type;
  }

  public void setType(Class<?> type) {
    this.type = type;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public boolean isCreateable() {
    return createable;
  }

  public void setCreateable(boolean createable) {
    this.createable = createable;
  }

  public boolean isUpdateable() {
    return updateable;
  }

  public void setUpdateable(boolean updateable) {
    this.updateable = updateable;
  }

  public boolean isSortable() {
    return sortable;
  }

  public void setSortable(boolean sortable) {
    this.sortable = sortable;
  }

  public Integer getMinLength() {
    return minLength;
  }

  public void setMinLength(Integer minLength) {
    this.minLength = minLength;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(Integer maxLength) {
    this.maxLength = maxLength;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public Collection<Annotation> getAnnotations() {
    return annotations.values();
  }

  public <T extends Annotation> boolean has(Class<T> annotation) {
    return annotations.containsKey(annotation);
  }

  public void addAnnotation(Annotation annotation) {
    this.annotations.put(annotation.annotationType(), annotation);
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
