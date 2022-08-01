package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebean.bean.ObjectEntity;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Bindable for a single BeanProperty.
 */
class BindableObjectProperty implements Bindable {
  private final boolean bindEncryptDataFirst;
  final BeanProperty prop;

  BindableObjectProperty(BeanProperty prop, boolean bindEncryptDataFirst) {
    this.prop = prop;
    this.bindEncryptDataFirst = bindEncryptDataFirst;
  }

  @Override
  public final String toString() {
    return prop.toString();
  }

  @Override
  public final boolean isDraftOnly() {
    return prop.isDraftOnly();
  }

  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    EntityBean bean = request.entityBean();
    if (bean instanceof ObjectEntity) {
      ObjectEntity entity = (ObjectEntity) bean;
      Set<String> dirtySet = entity.dirtySet();
      if (dirtySet != null && dirtySet.contains(prop.name())) {
        list.add(this);
      }
    } else if (request.isAddToUpdate(prop)) {
      list.add(this);
    }
  }

  @Override
  public final void dmlAppend(GenerateDmlRequest request) {
    request.appendColumn(prop.dbColumn());
  }

  /**
   * Normal binding of a property value from the bean.
   */
  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {
    Object value = null;
    if (bean != null) {
      value = prop.getValue(bean);
    }
    request.bind(value, prop);
  }

}
