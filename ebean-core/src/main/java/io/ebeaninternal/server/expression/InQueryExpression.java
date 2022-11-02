package io.ebeaninternal.server.expression;

import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.*;
import io.ebeaninternal.api.SpiQuery.Type;
import io.ebeaninternal.server.query.CQuery;

import java.util.List;

/**
 * In expression using a sub query.
 */
final class InQueryExpression extends AbstractExpression implements UnsupportedDocStoreExpression {

  private final boolean not;
  private final SpiQuery<?> subQuery;
  private List<Object> bindParams;
  private String sql;

  InQueryExpression(String propertyName, SpiQuery<?> subQuery, boolean not) {
    super(propertyName);
    this.subQuery = subQuery;
    this.not = not;
  }

  InQueryExpression(String propertyName, boolean not, String sql, List<Object> bindParams) {
    super(propertyName);
    this.subQuery = null;
    this.not = not;
    this.sql = sql;
    this.bindParams = bindParams;
  }

  @Override
  public SpiExpression copy() {
    return subQuery == null ? this : new InQueryExpression(propName, subQuery.copy(), not);
  }

  @Override
  public void simplify() {
    // do nothing
  }

  @Override
  public void writeDocQuery(DocQueryContext context) {
    throw new IllegalStateException("Not supported");
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    CQuery<?> subQuery = compileSubQuery(request);
    this.bindParams = subQuery.predicates().whereExprBindValues();
    this.sql = subQuery.generatedSql().replace('\n', ' ');
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("InQuery[").append(propName)
      .append(" not:").append(not).append(" sql:").append(sql)
      .append(" ?:").append(bindParams.size()).append("]");
  }

  /**
   * Compile/build the sub query.
   */
  private CQuery<?> compileSubQuery(BeanQueryRequest<?> queryRequest) {
    SpiEbeanServer ebeanServer = (SpiEbeanServer) queryRequest.database();
    return ebeanServer.compileQuery(Type.SQ_IN, subQuery, queryRequest.transaction());
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    subQuery.queryBindKey(key);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    request.append(propName);
    if (not) {
      request.append(" not");
    }
    request.append(" in (");
    request.append(sql);
    request.append(")");
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    for (Object bindParam : bindParams) {
      request.addBindValue(bindParam);
    }
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    InQueryExpression that = (InQueryExpression) other;
    if (this.bindParams.size() != that.bindParams.size()) {
      return false;
    }
    for (int i = 0; i < bindParams.size(); i++) {
      if (!bindParams.get(i).equals(that.bindParams.get(i))) {
        return false;
      }
    }
    return true;
  }
}
