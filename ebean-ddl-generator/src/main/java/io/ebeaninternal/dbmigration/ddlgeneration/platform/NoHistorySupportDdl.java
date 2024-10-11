package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.DatabaseBuilder;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.model.MTable;

/**
 * Default history implementation that does nothing. Needs to be replaced
 * with an appropriate implementation for the given database platform.
 */
public class NoHistorySupportDdl implements PlatformHistoryDdl {

  @Override
  public void configure(DatabaseBuilder.Settings config, PlatformDdl platformDdl) {
    // does nothing
  }

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) {
    // does nothing
  }

  @Override
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) {
    // does nothing
  }

  @Override
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) {
    // does nothing
  }

}
