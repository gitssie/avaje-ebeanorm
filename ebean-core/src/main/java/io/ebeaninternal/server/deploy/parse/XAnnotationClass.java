package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.ChangeLog;
import io.ebean.config.TableName;
import io.ebean.event.changelog.ChangeLogFilter;
import io.ebeaninternal.server.changelog.DefaultChangeLogRegister;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;

import javax.persistence.Table;

final class XAnnotationClass {
  private final XEntity entity;
  private final DeployBeanDescriptor<?> descriptor;
  private final String asOfViewSuffix;
  private final String versionsBetweenSuffix;

  XAnnotationClass(XEntity entity, DeployBeanInfo<?> info, ReadAnnotationConfig readConfig) {
    this.entity = entity;
    this.descriptor = info.getDescriptor();
    this.asOfViewSuffix = readConfig.getAsOfViewSuffix();
    this.versionsBetweenSuffix = readConfig.getVersionsBetweenSuffix();
  }

  public void parse() {
    setTableName();
    read(descriptor.getBeanType());

  }

  private void read(Class<?> cls) {
    descriptor.setName(entity.getName());

    //changelog
    ChangeLog changeLog = entity.getAnnotation(ChangeLog.class);
    if (changeLog != null) {
      boolean includeInserts = descriptor.getConfig().isChangeLogIncludeInserts();
      DefaultChangeLogRegister changeLogRegister = new DefaultChangeLogRegister(includeInserts);
      ChangeLogFilter changeLogFilter = changeLogRegister.getChangeFilter(changeLog);
      if (changeLogFilter != null) {
        descriptor.setChangeLogFilter(changeLogFilter);
      }
    }
  }

  private void setTableName() {
    Table table = entity.getAnnotation(Table.class);
    if (descriptor.isBaseTableType() && table != null) {
      // default the TableName using NamingConvention.
      TableName tableName = new TableName((table.catalog()), (table.schema()), (table.name()));
      descriptor.setBaseTable(tableName, asOfViewSuffix, versionsBetweenSuffix);
    }
  }
}
