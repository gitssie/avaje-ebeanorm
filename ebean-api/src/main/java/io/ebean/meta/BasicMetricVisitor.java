package io.ebean.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A simple MetricVisitor that can collect the desired metrics into lists.
 */
public class BasicMetricVisitor extends AbstractMetricVisitor implements ServerMetrics {

  private final String name;
  private final Function<String,String> naming;
  private final List<MetaTimedMetric> timed = new ArrayList<>();
  private final List<MetaQueryMetric> query = new ArrayList<>();
  private final List<MetaCountMetric> count = new ArrayList<>();

  public BasicMetricVisitor() {
    this("db", MetricNamingMatch.INSTANCE);
  }

  /**
   * Construct to reset and collect everything.
   */
  public BasicMetricVisitor(String name, Function<String,String> naming) {
    this(name, naming, true, true, true, true);
  }

  /**
   * Construct specifying reset and what to collect.
   */
  public BasicMetricVisitor(String name, Function<String,String> naming, boolean reset, boolean collectTransactionMetrics, boolean collectQueryMetrics, boolean collectL2Metrics) {
    super(reset, collectTransactionMetrics, collectQueryMetrics, collectL2Metrics);
    this.name = name;
    this.naming = naming;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Function<String, String> namingConvention() {
    return naming;
  }

  @Override
  public ServerMetricsAsJson asJson() {
    return new MetricsAsJson(this);
  }

  @Override
  public List<MetricData> asData() {
    return new MetricsAsData(this).data();
  }

  @Override
  public List<MetaTimedMetric> timedMetrics() {
    return timed;
  }

  @Override
  public List<MetaQueryMetric> queryMetrics() {
    return query;
  }

  @Override
  public List<MetaCountMetric> countMetrics() {
    return count;
  }

  @Override
  public void visitTimed(MetaTimedMetric metric) {
    timed.add(metric);
  }

  @Override
  public void visitQuery(MetaQueryMetric metric) {
    query.add(metric);
  }

  @Override
  public void visitCount(MetaCountMetric metric) {
    count.add(metric);
  }
}
