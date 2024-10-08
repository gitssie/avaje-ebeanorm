package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.annotation.ConstraintMode;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;

public class NuoDbDdl extends PlatformDdl {

  public NuoDbDdl(DatabasePlatform platform) {
    super(platform);
    this.historyDdl = new NuoDbHistoryDdl();
    this.identitySuffix = " generated by default as identity";
    this.dropConstraintIfExists = "drop constraint";
  }

  @Override
  public String createSequence(String sequenceName, DdlIdentity identity) {
    StringBuilder sb = new StringBuilder("create sequence ");
    sb.append(quote(sequenceName));
    int start = identity.getStart();
    if (start > 0) {
      sb.append(" ").append(sequenceStartWith).append(" ").append(start);
    }
    int cache = identity.getCache();
    if (cache > 0) {
      sb.append(" quantum size ").append(cache);
    }
    sb.append(";");
    return sb.toString();
  }

  @Override
  public void addTableComment(DdlBuffer apply, String tableName, String tableComment) {
    // do nothing
  }

  @Override
  public void addColumnComment(DdlBuffer apply, String table, String column, String comment) {
    // do nothing
  }

  @Override
  protected void appendForeignKeyOnUpdate(StringBuilder buffer, ConstraintMode mode) {
    // do nothing
  }

  @Override
  protected void appendForeignKeyOnDelete(StringBuilder buffer, ConstraintMode mode) {
    if (mode == ConstraintMode.RESTRICT) {
      // do nothing
    } else if (mode == ConstraintMode.SET_NULL) {
      // do nothing
    } else {
      super.appendForeignKeyOnDelete(buffer, mode);
    }
  }
}
