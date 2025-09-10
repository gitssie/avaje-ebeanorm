package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import javax.persistence.ConstraintMode;
import javax.persistence.ForeignKey;
import java.lang.annotation.Annotation;

/**
 * @author: Awesome
 * @create: 2024-03-27 11:19
 */
public class XForeignKey implements ForeignKey {
  private String name = "";
  private String foreignKeyDefinition = "";
  private ConstraintMode value = ConstraintMode.NO_CONSTRAINT;
  @Override
  public String name() {
    return name;
  }

  @Override
  public String foreignKeyDefinition() {
    return foreignKeyDefinition;
  }

  @Override
  public ConstraintMode value() {
    return value;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return ForeignKey.class;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getForeignKeyDefinition() {
    return foreignKeyDefinition;
  }

  public void setForeignKeyDefinition(String foreignKeyDefinition) {
    this.foreignKeyDefinition = foreignKeyDefinition;
  }

  public ConstraintMode getValue() {
    return value;
  }

  public void setValue(ConstraintMode value) {
    this.value = value;
  }
}
