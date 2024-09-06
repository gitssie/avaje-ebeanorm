package io.ebeaninternal.server.query;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.api.SpiQuery.Mode;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.DbReadContext;

/**
 * Controls the loading of property data into a bean.
 * <p>
 * Takes into account the differences of lazy loading and
 * partial objects.
 * </p>
 */
public final class SqlBeanLoad {
  private final DbReadContext ctx;
  private final EntityBean bean;
  private final EntityBeanIntercept ebi;
  private EntityBeanIntercept ebiElement;
  private final Class<?> type;
  private final boolean lazyLoading;
  private final boolean rawSql;

  SqlBeanLoad(STreeType desc, DbReadContext ctx, Class<?> type, EntityBean bean, Mode queryMode) {
    this.ctx = ctx;
    this.rawSql = ctx.isRawSql();
    this.type = type;
    this.lazyLoading = queryMode == Mode.LAZYLOAD_BEAN;
    this.bean = bean;
    this.ebi = bean == null ? null : bean._ebean_getIntercept();
    if(bean != null && desc instanceof BeanDescriptor){
      EntityBean elementBean = ((BeanDescriptor<?>) desc).elementBean(bean);
      if(elementBean != null){
        ebiElement = elementBean._ebean_getIntercept();
      }
    }
  }

  /**
   * Return true if this is a lazy loading.
   */
  public boolean isLazyLoad() {
    return lazyLoading;
  }

  /**
   * Return the DB read context.
   */
  public DbReadContext ctx() {
    return ctx;
  }

  private boolean isLoadedProperty(BeanProperty prop) {
    if (prop.isCustom()) {
      return ebiElement == null ? false : ebiElement.isLoadedProperty(prop.fieldIndex()[1]);
    }else{
      return ebi.isLoadedProperty(prop.propertyIndex());
    }
  }

  public Object load(BeanProperty prop) {
    if (!rawSql && !prop.isLoadProperty(ctx.isDraftQuery())) {
      return null;
    }
    if ((bean == null)
      || (lazyLoading && isLoadedProperty(prop))
      || (type != null && !prop.isAssignableFrom(type))) {
      // ignore this property
      // ... null: bean already in persistence context
      // ... lazyLoading: partial bean that is lazy loading
      // ... type: inheritance and not assignable to this instance
      prop.loadIgnore(ctx);
      return null;
    }
    try {
      return prop.readSet(ctx, bean);
    } catch (Exception e) {
      bean._ebean_getIntercept().setLoadError(prop.propertyIndex(), e);
      ctx.handleLoadError(prop.fullName(), e);
      return prop.getValue(bean);
    }
  }

  /**
   * Load the given value into the property.
   */
  public void load(BeanProperty target, Object dbVal) {
    target.setValue(bean, dbVal);
  }
}
