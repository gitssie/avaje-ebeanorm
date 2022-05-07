package io.ebean.bean;

import io.ebeaninternal.server.deploy.parse.tenant.XEntityFinder;

public interface XEntityProvider {
  XEntityFinder create();
}
