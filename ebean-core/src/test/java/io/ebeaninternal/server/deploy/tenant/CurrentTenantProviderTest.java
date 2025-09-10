package io.ebeaninternal.server.deploy.tenant;

import io.ebean.config.CurrentTenantProvider;

public class CurrentTenantProviderTest implements CurrentTenantProvider {
  @Override
  public Object currentId() {
    return 1;
  }
}
