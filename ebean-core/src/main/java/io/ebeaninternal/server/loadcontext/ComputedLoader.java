package io.ebeaninternal.server.loadcontext;

import io.ebean.CacheMode;
import io.ebean.bean.*;
import io.ebean.common.LazyComputed;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;

import java.util.Optional;


public class ComputedLoader implements BeanCollectionLoader {
  private SpiEbeanServer server;

  public ComputedLoader(SpiEbeanServer server) {
    this.server = server;
  }

  @Override
  public String name() {
    return server.name();
  }

  @Override
  public void loadMany(BeanCollection<?> collection, boolean onlyIds) {
    loadValueInternal(collection, collection.owner(), collection.propertyName(), true, onlyIds);
  }

  private void loadValueInternal(BeanCollection collection, EntityBean parentBean, String propertyName, boolean refresh, boolean onlyIds) {
    EntityBeanIntercept ebi = parentBean._ebean_getIntercept();
    PersistenceContext pc = ebi.persistenceContext();
    BeanDescriptor<?> parentDesc = (BeanDescriptor<?>) server.pluginApi().beanType(parentBean.getClass());
    BeanProperty many = parentDesc.beanProperty(propertyName);

    Object parentId = parentDesc.getId(parentBean);
    if (pc == null) {
      pc = new DefaultPersistenceContext();
      parentDesc.contextPut(pc, parentId, parentBean);
    }

    SpiQuery<?> query = (SpiQuery<?>) server.createQuery(parentDesc.type());
    query.setLoadDescription("+lazy", null);
    query.select(propertyName);
    query.where().idEq(parentId);
    query.setBeanCacheMode(CacheMode.OFF);
    query.setMode(SpiQuery.Mode.LAZYLOAD_COMPUTED);
    query.setPersistenceContext(pc);

    Object value = query.asDto(many.scalarType().type()).findOne();
    //set lazy value
    collection.addBean(value);
  }

}
