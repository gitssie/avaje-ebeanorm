package io.ebeaninternal.server.core.adapter;

import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanInit;
import io.ebean.meta.QueryPlanRequest;
import io.ebeaninternal.api.SpiEbeanServer;

import java.util.List;

public interface SpiServerAdapter {

   List<MetaQueryPlan> queryPlanInit(QueryPlanInit initRequest);

   List<MetaQueryPlan> queryPlanCollectNow(QueryPlanRequest request);

   SpiEbeanServer getServer();
}
