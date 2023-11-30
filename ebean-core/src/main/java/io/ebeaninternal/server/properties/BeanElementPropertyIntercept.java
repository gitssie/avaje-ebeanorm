package io.ebeaninternal.server.properties;

import io.ebean.ValuePair;
import io.ebean.bean.*;

import java.util.Map;
import java.util.Set;

/**
 * This is the object added to every entity bean using byte code enhancement.
 * <p>
 * This provides the mechanisms to support deferred fetching of reference beans
 * and oldValues generation for concurrency checking.
 * </p>
 */
final class BeanElementPropertyIntercept implements EntityBeanIntercept {

  private final EntityBeanIntercept proxy;

  public BeanElementPropertyIntercept(EntityBeanIntercept proxy) {
    this.proxy = proxy;
  }

  @Override
  public EntityBean getOwner() {
    return proxy.getOwner();
  }

  @Override
  public PersistenceContext getPersistenceContext() {
    return proxy.getPersistenceContext();
  }

  @Override
  public void setPersistenceContext(PersistenceContext persistenceContext) {
    proxy.setPersistenceContext(persistenceContext);
  }

  @Override
  public void setNodeUsageCollector(NodeUsageCollector usageCollector) {
    proxy.setNodeUsageCollector(usageCollector);
  }

  @Override
  public Object getOwnerId() {
    return proxy.getOwnerId();
  }

  @Override
  public void setOwnerId(Object ownerId) {
    proxy.setOwnerId(ownerId);
  }

  @Override
  public Object getEmbeddedOwner() {
    return proxy.getEmbeddedOwner();
  }

  @Override
  public int getEmbeddedOwnerIndex() {
    return proxy.getEmbeddedOwnerIndex();
  }

  @Override
  public void clearGetterCallback() {
    proxy.clearGetterCallback();
  }

  @Override
  public void registerGetterCallback(PreGetterCallback getterCallback) {
    proxy.registerGetterCallback(getterCallback);
  }

  @Override
  public void setEmbeddedOwner(EntityBean parentBean, int embeddedOwnerIndex) {
    proxy.setEmbeddedOwner(parentBean, embeddedOwnerIndex);
  }

  @Override
  public void setBeanLoader(BeanLoader beanLoader, PersistenceContext ctx) {
    proxy.setBeanLoader(beanLoader, ctx);
  }

  @Override
  public void setBeanLoader(BeanLoader beanLoader) {
    proxy.setBeanLoader(beanLoader);
  }

  @Override
  public boolean isFullyLoadedBean() {
    return proxy.isFullyLoadedBean();
  }

  @Override
  public void setFullyLoadedBean(boolean fullyLoadedBean) {
    proxy.setFullyLoadedBean(fullyLoadedBean);
  }

  @Override
  public boolean isPartial() {
    return proxy.isPartial();
  }

  @Override
  public boolean isDirty() {
    return proxy.isDirty();
  }

  @Override
  public void setEmbeddedDirty(int embeddedProperty) {
    proxy.setEmbeddedDirty(embeddedProperty);
  }

  @Override
  public void setDirty(boolean dirty) {
    proxy.setDirty(dirty);
  }

  @Override
  public boolean isNew() {
    return proxy.isNew();
  }

  @Override
  public boolean isNewOrDirty() {
    return proxy.isNewOrDirty();
  }

  @Override
  public boolean hasIdOnly(int idIndex) {
    return proxy.hasIdOnly(idIndex);
  }

  @Override
  public boolean isReference() {
    return proxy.isReference();
  }

  @Override
  public void setReference(int idPos) {
    proxy.setReference(idPos);
  }

  @Override
  public void setLoadedFromCache(boolean loadedFromCache) {
    proxy.setLoadedFromCache(loadedFromCache);
  }

  @Override
  public boolean isLoadedFromCache() {
    return proxy.isLoadedFromCache();
  }

  @Override
  public boolean isReadOnly() {
    return proxy.isReadOnly();
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    proxy.setReadOnly(readOnly);
  }

  @Override
  public void setForceUpdate(boolean forceUpdate) {
    proxy.setForceUpdate(forceUpdate);
  }

  @Override
  public boolean isUpdate() {
    return proxy.isUpdate();
  }

  @Override
  public boolean isLoaded() {
    return proxy.isLoaded();
  }

  @Override
  public void setNew() {
    proxy.setNew();
  }

  @Override
  public void setLoaded() {
    proxy.setLoaded();
  }

  @Override
  public void setLoadedLazy() {
    proxy.setLoadedLazy();
  }

  @Override
  public void setLazyLoadFailure(Object ownerId) {
    proxy.setLazyLoadFailure(ownerId);
  }

  @Override
  public boolean isLazyLoadFailure() {
    return proxy.isLazyLoadFailure();
  }

  @Override
  public boolean isDisableLazyLoad() {
    return proxy.isDisableLazyLoad();
  }

  @Override
  public void setDisableLazyLoad(boolean disableLazyLoad) {
    proxy.setDisableLazyLoad(disableLazyLoad);
  }

  @Override
  public void setEmbeddedLoaded(Object embeddedBean) {
    proxy.setEmbeddedLoaded(embeddedBean);
  }

  @Override
  public boolean isEmbeddedNewOrDirty(Object embeddedBean) {
    return proxy.isEmbeddedNewOrDirty(embeddedBean);
  }

  @Override
  public Object getOrigValue(int propertyIndex) {
    return proxy.getOrigValue(propertyIndex);
  }

  @Override
  public int findProperty(String propertyName) {
    return proxy.findProperty(propertyName);
  }

  @Override
  public String getProperty(int propertyIndex) {
    return proxy.getProperty(propertyIndex);
  }

  @Override
  public int getPropertyLength() {
    return proxy.getPropertyLength();
  }

  @Override
  public void setPropertyLoaded(String propertyName, boolean loaded) {
    proxy.setPropertyLoaded(propertyName, loaded);
  }

  @Override
  public void setPropertyUnloaded(int propertyIndex) {
    proxy.setPropertyUnloaded(propertyIndex);
  }

  @Override
  public void setLoadedProperty(int propertyIndex) {
    proxy.setLoadedProperty(propertyIndex);
  }

  @Override
  public void setLoadedPropertyAll() {
    proxy.setLoadedPropertyAll();
  }

  @Override
  public boolean isLoadedProperty(int propertyIndex) {
    return proxy.isLoadedProperty(propertyIndex);
  }

  @Override
  public boolean isChangedProperty(int propertyIndex) {
    return proxy.isChangedProperty(propertyIndex);
  }

  @Override
  public boolean isDirtyProperty(int propertyIndex) {
    return proxy.isDirtyProperty(propertyIndex);
  }

  @Override
  public void markPropertyAsChanged(int propertyIndex) {
    proxy.markPropertyAsChanged(propertyIndex);
  }

  @Override
  public void setChangedProperty(int propertyIndex) {
    proxy.setChangedProperty(propertyIndex);
  }

  @Override
  public void setChangeLoaded(int propertyIndex) {
    proxy.setChangeLoaded(propertyIndex);
  }

  @Override
  public void setEmbeddedPropertyDirty(int propertyIndex) {
    proxy.setEmbeddedPropertyDirty(propertyIndex);
  }

  @Override
  public void setOriginalValue(int propertyIndex, Object value) {
    proxy.setOriginalValue(propertyIndex, value);
  }

  @Override
  public void setOriginalValueForce(int propertyIndex, Object value) {
    proxy.setOriginalValueForce(propertyIndex, value);
  }

  @Override
  public void setNewBeanForUpdate() {
    proxy.setNewBeanForUpdate();
  }

  @Override
  public Set<String> getLoadedPropertyNames() {
    return proxy.getLoadedPropertyNames();
  }

  @Override
  public boolean[] getDirtyProperties() {
    return proxy.getDirtyProperties();
  }

  @Override
  public Set<String> getDirtyPropertyNames() {
    return proxy.getDirtyPropertyNames();
  }

  @Override
  public void addDirtyPropertyNames(Set<String> props, String prefix) {
    proxy.addDirtyPropertyNames(props, null);
  }

  @Override
  public boolean hasDirtyProperty(Set<String> propertyNames) {
    return proxy.hasDirtyProperty(propertyNames);
  }

  @Override
  public Map<String, ValuePair> getDirtyValues() {
    return proxy.getDirtyValues();
  }

  @Override
  public void addDirtyPropertyValues(Map<String, ValuePair> dirtyValues, String prefix) {
    proxy.addDirtyPropertyValues(dirtyValues, prefix);
  }

  @Override
  public void addDirtyPropertyValues(BeanDiffVisitor visitor) {
    proxy.addDirtyPropertyValues(visitor);
  }

  @Override
  public StringBuilder getDirtyPropertyKey() {
    return proxy.getDirtyPropertyKey();
  }

  @Override
  public void addDirtyPropertyKey(StringBuilder sb) {
    proxy.addDirtyPropertyKey(sb);
  }

  @Override
  public StringBuilder getLoadedPropertyKey() {
    return proxy.getLoadedPropertyKey();
  }

  @Override
  public boolean[] getLoaded() {
    return proxy.getLoaded();
  }

  @Override
  public int getLazyLoadPropertyIndex() {
    return proxy.getLazyLoadPropertyIndex();
  }

  @Override
  public String getLazyLoadProperty() {
    return proxy.getLazyLoadProperty();
  }

  @Override
  public void loadBean(int loadProperty) {
    proxy.loadBean(loadProperty);
  }

  @Override
  public void loadBeanInternal(int loadProperty, BeanLoader loader) {
    proxy.loadBeanInternal(loadProperty, loader);
  }

  @Override
  public void initialisedMany(int propertyIndex) {
    proxy.initialisedMany(propertyIndex);
  }

  @Override
  public void preGetterCallback(int propertyIndex) {
    proxy.preGetterCallback(propertyIndex);
  }

  @Override
  public void preGetId() {
    proxy.preGetId();
  }

  @Override
  public void preGetter(int propertyIndex) {
    proxy.preGetter(propertyIndex);
  }

  @Override
  public void preSetterMany(boolean interceptField, int propertyIndex, Object oldValue, Object newValue) {
    proxy.preSetterMany(interceptField, propertyIndex, oldValue, newValue);
  }

  @Override
  public void setChangedPropertyValue(int propertyIndex, boolean setDirtyState, Object origValue) {
    proxy.setChangedPropertyValue(propertyIndex, setDirtyState, origValue);
  }

  @Override
  public void setDirtyStatus() {
    proxy.setDirtyStatus();
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, Object oldValue, Object newValue) {
    proxy.preSetter(intercept, propertyIndex, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, boolean oldValue, boolean newValue) {
    proxy.preSetter(intercept, propertyIndex, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, int oldValue, int newValue) {
    proxy.preSetter(intercept, propertyIndex, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, long oldValue, long newValue) {
    proxy.preSetter(intercept, propertyIndex, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, double oldValue, double newValue) {
    proxy.preSetter(intercept, propertyIndex, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, float oldValue, float newValue) {
    proxy.preSetter(intercept, propertyIndex, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, short oldValue, short newValue) {
    proxy.preSetter(intercept, propertyIndex, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, char oldValue, char newValue) {
    proxy.preSetter(intercept, propertyIndex, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, byte oldValue, byte newValue) {
    proxy.preSetter(intercept, propertyIndex, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, char[] oldValue, char[] newValue) {
    proxy.preSetter(intercept, propertyIndex, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, byte[] oldValue, byte[] newValue) {
    proxy.preSetter(intercept, propertyIndex, oldValue, newValue);
  }

  @Override
  public void setOldValue(int propertyIndex, Object oldValue) {
    proxy.setOldValue(propertyIndex, oldValue);
  }

  @Override
  public int getSortOrder() {
    return proxy.getSortOrder();
  }

  @Override
  public void setSortOrder(int sortOrder) {
    proxy.setSortOrder(sortOrder);
  }

  @Override
  public void setDeletedFromCollection(boolean deletedFromCollection) {
    proxy.setDeletedFromCollection(deletedFromCollection);
  }

  @Override
  public boolean isOrphanDelete() {
    return proxy.isOrphanDelete();
  }

  @Override
  public void setLoadError(int propertyIndex, Exception t) {
    proxy.setLoadError(propertyIndex, t);
  }

  @Override
  public Map<String, Exception> getLoadErrors() {
    return proxy.getLoadErrors();
  }

  @Override
  public boolean isChangedProp(int i) {
    return proxy.isChangedProp(i);
  }

  @Override
  public MutableValueInfo mutableInfo(int propertyIndex) {
    return proxy.mutableInfo(propertyIndex);
  }

  @Override
  public void mutableInfo(int propertyIndex, MutableValueInfo info) {
    proxy.mutableInfo(propertyIndex, info);
  }

  @Override
  public void mutableNext(int propertyIndex, MutableValueNext next) {
    proxy.mutableNext(propertyIndex, next);
  }

  @Override
  public String mutableNext(int propertyIndex) {
    return proxy.mutableNext(propertyIndex);
  }
}
