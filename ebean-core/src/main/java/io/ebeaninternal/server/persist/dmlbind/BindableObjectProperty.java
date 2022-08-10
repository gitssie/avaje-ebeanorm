package io.ebeaninternal.server.persist.dmlbind;

import io.ebean.bean.EntityBean;
import io.ebean.bean.ObjectEntity;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.BeanPropertyJsonMapper;
import io.ebeaninternal.server.persist.dml.DmlMode;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

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

  BindableObjectProperty(BeanProperty prop, DmlMode mode, boolean bindEncryptDataFirst, boolean allowManyToOne) {
    this.prop = prop;
    if (prop.isDbEncrypted()) {
      proxy = new BindableEncryptedProperty(prop, bindEncryptDataFirst);
    } else if (allowManyToOne && prop instanceof BeanPropertyAssocOne) {
      proxy = new BindableAssocOne((BeanPropertyAssocOne<?>) prop);
    } else if (prop instanceof BeanPropertyJsonMapper) {
      if (DmlMode.INSERT == mode) {
        proxy = new BindablePropertyJsonInsert(prop);
      } else if (DmlMode.UPDATE == mode) {
        proxy = new BindablePropertyJsonUpdate(prop);
      } else {
        proxy = new BindableProperty(prop);
      }
    } else {
      proxy = new BindableProperty(prop);
    }
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
    if (bean != null && bean instanceof ObjectEntity) {
      ObjectEntity entity = (ObjectEntity) bean;
      Set<String> dirtySet = entity.dirtySet();
      if (dirtySet != null && dirtySet.contains(prop.name())) {
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
