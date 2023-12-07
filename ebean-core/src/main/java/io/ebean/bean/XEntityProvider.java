package io.ebean.bean;

import io.ebean.config.CurrentTenantProvider;
import io.ebeaninternal.server.deploy.parse.tenant.XEntityFinder;

public interface XEntityProvider {
  XEntityFinder create();

  default CurrentTenantProvider tenantProvider() {
    return null;
  }
}
