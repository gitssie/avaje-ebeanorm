package io.ebeaninternal.server.deploy.parse.tenant.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;

public class EmptyGeneratedProperty implements GeneratedProperty {
  @Override
  public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
    return null;
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
    return false;
  }

  @Override
  public boolean isDDLNotNullable() {
    return false;
  }
}
