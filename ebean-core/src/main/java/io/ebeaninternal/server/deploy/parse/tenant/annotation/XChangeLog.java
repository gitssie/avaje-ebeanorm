package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.annotation.ChangeLog;
import io.ebean.annotation.ChangeLogInsertMode;

import java.lang.annotation.Annotation;

/**
 * @create: 2024-07-16 11:26
 */
public class XChangeLog implements ChangeLog {

  private ChangeLogInsertMode inserts = ChangeLogInsertMode.DEFAULT;
  private String[] updatesThatInclude = new String[0];


  @Override
  public ChangeLogInsertMode inserts() {
    return inserts;
  }

  @Override
  public String[] updatesThatInclude() {
    return updatesThatInclude;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return ChangeLog.class;
  }

  public ChangeLogInsertMode getInserts() {
    return inserts;
  }

  public void setInserts(ChangeLogInsertMode inserts) {
    this.inserts = inserts;
  }

  public String[] getUpdatesThatInclude() {
    return updatesThatInclude;
  }

  public void setUpdatesThatInclude(String[] updatesThatInclude) {
    this.updatesThatInclude = updatesThatInclude;
  }
}
