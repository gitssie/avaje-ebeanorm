package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.CurrentUserProvider;
import io.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to populate @TenantId bean properties.
 */
final class GeneratedWhoTenant implements GeneratedProperty {

  private final CurrentTenantProvider currentTenantProvider;

  GeneratedWhoTenant(CurrentTenantProvider currentTenantProvider) {
    this.currentTenantProvider = currentTenantProvider;
  }

  @Override
  public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
    Object tenantId = prop.value(bean);
    if (tenantId == null) {
      return currentTenantProvider.currentId();
    } else {
      return tenantId;
    }
  }

  @Override
  public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
    return null;
  }

  @Override
  public boolean includeInUpdate() {
    return false;
  }

  @Override
  public boolean includeInAllUpdates() {
    return false;
  }

  @Override
  public boolean includeInInsert() {
    return true;
  }

  @Override
  public boolean isDDLNotNullable() {
    return true;
  }
}
