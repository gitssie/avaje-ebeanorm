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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Bindable for a single BeanProperty.
 */
class BindableObjectProperty implements Bindable {
  private final BeanProperty prop;
  private final Bindable proxy;
  private final int[] fieldIndex;

  BindableObjectProperty(BeanProperty prop, Bindable proxy) {
    this.prop = prop;
    this.proxy = proxy;
    this.fieldIndex = prop.fieldIndex();
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
    ElementBean value = (ElementBean) bean._ebean_getField(fieldIndex[0]);
    if (value != null) {
      if (value._ebean_getPropertyNames().length == 0) {
        prop.value(bean);
      }
      if (value._ebean_getIntercept().isDirtyProperty(fieldIndex[1])) {
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
