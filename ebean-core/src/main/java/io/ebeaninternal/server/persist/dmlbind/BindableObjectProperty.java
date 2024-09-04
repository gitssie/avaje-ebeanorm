package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.ElementBean;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import javax.persistence.PersistenceException;
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
    ElementBean value = (ElementBean) bean._ebean_getField(prop.fieldIndex()[0]);
    if (value != null) {
      EntityBeanIntercept ebi = bean._ebean_getIntercept();
      EntityBeanIntercept ei = value._ebean_getIntercept();
      if (request.type() == PersistRequest.Type.UPDATE && ebi.isNew()) {
        list.add(this);
      } else if (ei.getPropertyLength() > 0 && ei.isDirtyProperty(prop.fieldIndex()[1])) {
        list.add(this);
      }
    } else {
      throw new PersistenceException("ElementBean is null in bean:" + bean);
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
