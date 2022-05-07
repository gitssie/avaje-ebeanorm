package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import java.lang.annotation.Annotation;

public class XJoinColumn implements JoinColumn {
  private String name;
  private String referencedColumnName = "";
  private boolean unique = false;
  private boolean nullable= true;
  private boolean insertable= true;
  private boolean updatable= true;
  private String columnDefinition= "";
  private String table = "";
  private ForeignKey foreignKey;

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
}
