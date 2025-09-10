package io.ebean.common;

import io.ebean.ExpressionList;
import io.ebean.bean.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;


public final class LazyComputed<T> implements Computed<T>, BeanCollection<T> {
  private static final long serialVersionUID = 3365725236140187599L;
  protected boolean readOnly = true;
  protected boolean disableLazyLoad;
  /**
   * The Database this is associated with. (used for lazy fetch).
   */
  protected transient BeanCollectionLoader loader;

  /**
   * Flag set when registered with the batch loading context.
   */
  protected boolean registeredWithLoadContext;
  protected String ebeanServerName;
  /**
   * The owning bean (used for lazy fetch).
   */
  protected EntityBean ownerBean;
  /**
   * The name of this property in the owning bean (used for lazy fetch).
   */
  protected String propertyName;

  protected Object[] value;

  public LazyComputed() {
    this.value = new Object[0];
  }

  public LazyComputed(T value) {
    this.value = new Object[]{value};
  }

  public LazyComputed(BeanCollectionLoader loader, EntityBean ownerBean, String propertyName) {
    this.loader = loader;
    this.ebeanServerName = loader.name();
    this.ownerBean = ownerBean;
    this.propertyName = propertyName;
    this.readOnly = ownerBean != null && ownerBean._ebean_getIntercept().isReadOnly();
  }

  public void setDisableLazyLoad(boolean disableLazyLoad) {
    this.disableLazyLoad = disableLazyLoad;
  }

  @Override
  public void loadFrom(BeanCollection<?> other) {

  }

  @Override
  public void addBean(T bean) {
    this.value = bean == null ? new Object[0] : new Object[]{bean};
  }

  @Override
  public void removeBean(T bean) {
    this.value = new Object[0];
  }

  private void lazyLoadValue(boolean onlyIds) {
    if (loader == null) {
      throw new IllegalStateException("Lazy Computed Loader is null");
    }
    loader.loadMany(this, onlyIds);
  }

  public void setLoader(BeanCollectionLoader loader) {
    this.registeredWithLoadContext = true;
    this.loader = loader;
    this.ebeanServerName = loader.name();
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    this.readOnly = true;
  }

  public boolean isRegisteredWithLoadContext() {
    return registeredWithLoadContext;
  }


  public boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public void internalAdd(Object bean) {

  }

  @Override
  public void internalAddWithCheck(Object bean) {

  }

  @Override
  public T get() {
    init();
    if (value.length == 1) {
      return (T) value[0];
    } else {
      return null;
    }
  }

  @Override
  public int size() {
    init();
    return value == null ? 0 : value.length;
  }

  @Override
  public boolean isEmpty() {
    init();
    return value == null || value.length == 0;
  }

  @Override
  public Collection<T> getActualDetails() {
    return Collections.EMPTY_LIST;
  }

  @Override
  public Collection<?> getActualEntries() {
    return Collections.EMPTY_LIST;
  }

  @Override
  public boolean isPopulated() {
    return false;
  }

  @Override
  public boolean isReference() {
    return value == null;
  }

  @Override
  public boolean hasModifications() {
    return false;
  }

  @Override
  public void setModifyListening(ModifyListenMode modifyListenMode) {

  }

  @Override
  public ModifyListenMode getModifyListening() {
    return null;
  }

  @Override
  public void modifyAddition(T bean) {

  }

  @Override
  public void modifyRemoval(Object bean) {

  }

  @Override
  public Set<T> getModifyAdditions() {
    return null;
  }

  @Override
  public Set<T> getModifyRemovals() {
    return null;
  }

  @Override
  public void modifyReset() {

  }

  @Override
  public boolean wasTouched() {
    return false;
  }

  @Override
  public BeanCollection<T> getShallowCopy() {
    return null;
  }

  private synchronized void init() {
    if (value == null) {
      if (disableLazyLoad) {
        value = new Object[0];
      } else {
        lazyLoadValue(false);
      }
    }
  }

  public void reset(EntityBean ownerBean, String propertyName) {
    this.ownerBean = ownerBean;
    this.propertyName = propertyName;
    this.value = null;
  }

  @Override
  public boolean isSkipSave() {
    return false;
  }

  @Override
  public boolean holdsModifications() {
    return false;
  }

  @Override
  public EntityBean getOwnerBean() {
    return ownerBean;
  }

  @Override
  public String getPropertyName() {
    return propertyName;
  }

  @Override
  public boolean checkEmptyLazyLoad() {
    return false;
  }

  @Override
  public ExpressionList<?> getFilterMany() {
    return null;
  }

  @Override
  public void setFilterMany(ExpressionList<?> filterMany) {

  }

  @Override
  public void toString(ToStringBuilder builder) {
    builder.addRaw(getClass().getSimpleName());
  }
}
