package io.ebeaninternal.server.deploy;

import io.ebean.bean.BeanCollectionLoader;
import io.ebean.bean.EntityBean;
import io.ebean.bean.Computed;
import io.ebean.common.LazyComputed;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.loadcontext.ComputedLoader;

/**
 * Helper object for dealing with Computed value.
 */
public class BeanComputedHelp {

  final String propertyName;
  BeanCollectionLoader loader;

  public BeanComputedHelp(String propertyName) {
    this.propertyName = propertyName;
  }

  public final void setLoader(SpiEbeanServer loader) {
    this.loader = new ComputedLoader(loader);
  }

  public Computed<?> createReference(EntityBean localBean) {
    Computed<?> beanList = new LazyComputed<>(loader, localBean, propertyName);
    return beanList;
  }
}
