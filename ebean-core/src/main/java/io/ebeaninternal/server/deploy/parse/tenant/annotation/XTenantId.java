package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.annotation.TenantId;

import java.lang.annotation.Annotation;

public class XTenantId implements TenantId {
  public static final String NAME = "tenantId";

  @Override
  public Class<? extends Annotation> annotationType() {
    return TenantId.class;
  }
}
