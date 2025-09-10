package io.ebeaninternal.server.core.adapter;

import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanInit;
import io.ebean.meta.QueryPlanRequest;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.core.DefaultServer;

import java.util.List;

public class DefaultServerAdapter implements SpiServerAdapter {
  private final DefaultServer server;

  public DefaultServerAdapter(DefaultServer server) {
    this.server = server;
  }

  @Override
  public List<MetaQueryPlan> queryPlanInit(QueryPlanInit initRequest) {
    return null;
  }

  @Override
  public List<MetaQueryPlan> queryPlanCollectNow(QueryPlanRequest request) {
    return null;
  }

  @Override
  public SpiEbeanServer getServer() {
    return server;
  }

}
