package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.DbSqlContext;

import java.util.List;

final class SqlTreeNodeManyRoot extends SqlTreeNodeBean {

  final STreePropertyAssocMany manyProp;

  SqlTreeNodeManyRoot(String prefix, STreePropertyAssocMany prop, SqlTreeProperties props, List<SqlTreeNode> myList,
                      boolean withId, SpiQuery.TemporalMode temporalMode, boolean disableLazyLoad, boolean readOnly) {
    super(prefix, prop, props, myList, withId, temporalMode, disableLazyLoad, readOnly);
    this.manyProp = prop;
  }

  @Override
  public SqlTreeLoad createLoad() {
    return new SqlTreeLoadManyRoot(this);
  }

  @Override
  public boolean hasMany() {
    return true;
  }

  /**
   * Append the property columns to the buffer.
   */
  @Override
  public void appendDistinctOn(DbSqlContext ctx, boolean subQuery) {
    ctx.pushTableAlias(prefix);
    appendSelectId(ctx, idBinder.getBeanProperty());
    ctx.popTableAlias();
  }

  @Override
  protected void appendExtraWhere(DbSqlContext ctx) {
    // extraWhere is already appended to the tableJoin
  }

  /**
   * Force outer join for everything after the many property.
   */
  @Override
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
    super.appendFrom(ctx, joinType.autoToOuter());
  }
}
