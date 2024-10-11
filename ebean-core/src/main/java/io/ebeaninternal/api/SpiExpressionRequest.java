package io.ebeaninternal.api;

import io.ebeaninternal.server.core.SpiOrmQueryRequest;
import io.ebeaninternal.server.expression.platform.DbExpressionHandler;
import io.ebeaninternal.server.expression.platform.DbExpressionRequest;

import java.util.List;

/**
 * Request object used for gathering expression sql and bind values.
 */
public interface SpiExpressionRequest extends SpiExpressionBind, DbExpressionRequest {

  /**
   * Return the DB specific handler for JSON and ARRAY expressions.
   */
  DbExpressionHandler platformHandler();

  /**
   * Parse the logical property name to the deployment name.
   */
  String parseDeploy(String logicalProp);

  /**
   * Return the associated QueryRequest.
   */
  SpiOrmQueryRequest<?> queryRequest();

  /**
   * Append to the expression sql without any parsing.
   */
  @Override
  SpiExpressionRequest append(String expression);

  /**
   * Append to the expression sql without any parsing.
   */
  @Override
  SpiExpressionRequest append(char c);

  /**
   * Append to the expression sql with logical property parsing to db columns with logical path prefix.
   * <p>
   * This is a fast path case when expression is a bean property path and falls back to using parse()
   * when that isn't the case.
   */
  @Override
  SpiExpressionRequest property(String expression);

  /**
   * Append to the expression sql with logical property parsing to db columns with logical path prefix.
   */
  SpiExpressionRequest parse(String expression);

  /**
   * Return the accumulated expression sql for all expressions in this request.
   */
  String sql();

  /**
   * Return the ordered list of bind values for all expressions in this request.
   */
  List<Object> bindValues();

  /**
   * Increments the parameter index and returns that value.
   */
  int nextParameter();

  /**
   * Append a DB Like clause.
   */
  void appendLike(boolean rawLikeExpression);

  /**
   * Append IN expression taking into account platform and type support for Multi-value.
   */
  void appendInExpression(boolean not, List<Object> bindValues);
}
