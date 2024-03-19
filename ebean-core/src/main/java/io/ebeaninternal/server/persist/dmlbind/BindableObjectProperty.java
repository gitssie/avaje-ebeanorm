package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.ElementBean;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import java.sql.SQLException;
import java.util.List;

/**
 * Bindable for a single BeanProperty.
 */
class BindableObjectProperty implements Bindable {
  private final BeanProperty prop;
  private final Bindable proxy;

  BindableObjectProperty(BeanProperty prop, Bindable proxy) {
    this.prop = prop;
    this.proxy = proxy;
  }

  @Override
  public final String toString() {
    return proxy.toString();
  }

  @Override
  public final boolean isDraftOnly() {
    return proxy.isDraftOnly();
  }

  @Override
  public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list) {
    EntityBean bean = request.entityBean();
    Object value = bean._ebean_getField(prop.fieldIndex()[0]);
    if (value != null && (value instanceof ElementBean)) {
      ElementBean ebean = (ElementBean) value;
      EntityBeanIntercept ei = ebean._ebean_getIntercept();
      if (request.type() == PersistRequest.Type.UPDATE && ei.isNew()) {
        list.add(this);
      } else if (ei.getPropertyLength() > 0 && ei.isDirtyProperty(prop.fieldIndex()[1])) {
        list.add(this);
      }
    } else {
      proxy.addToUpdate(request, list);
    }
  }

  @Override
  public final void dmlAppend(GenerateDmlRequest request) {
    proxy.dmlAppend(request);
  }

  /**
   * Normal binding of a property value from the bean.
   */
  @Override
  public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException {
    proxy.dmlBind(request, bean);
  }
}
