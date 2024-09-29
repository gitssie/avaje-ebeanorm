package io.ebeaninternal.server.deploy.parse.tenant.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebean.core.type.BasicTypeConverter;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;

public class DefaultGeneratedProperty implements GeneratedProperty {
  @Override
  public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
    Object currVal = prop.getValue(bean);
    if(currVal != null){
      return currVal;
    }
    return BasicTypeConverter.convert(prop.dbColumnDefault(), prop.scalarType().jdbcType());
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
    return false;
  }
}
