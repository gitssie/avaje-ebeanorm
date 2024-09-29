
package io.ebeaninternal.server.query;

import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.SpiDbQueryPlan;
import io.ebeaninternal.api.SpiQueryPlan;
import io.ebeaninternal.server.bind.capture.BindCapture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.System.Logger.Level.WARNING;

/**
 * A QueryPlanLogger for oracle.
 *
 * @author Roland Praml, FOCONIS AG
 */
public final class QueryPlanLoggerOracle extends QueryPlanLogger {

  @Override
  public SpiDbQueryPlan collectPlan(Connection conn, SpiQueryPlan plan, BindCapture bind) {
    try (Statement stmt = conn.createStatement()) {
      try (PreparedStatement explainStmt = conn.prepareStatement("EXPLAIN PLAN FOR " + plan.sql())) {
        bind.prepare(explainStmt, conn);
        explainStmt.execute();
      }
      try (ResultSet rset = stmt.executeQuery("select plan_table_output from table(dbms_xplan.display())")) {
        return readQueryPlan(plan, bind, rset);
      }
    } catch (SQLException e) {
      CoreLog.log.log(WARNING, "Could not log query plan", e);
      return null;
    }
  }

}
