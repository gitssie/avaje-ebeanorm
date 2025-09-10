package io.ebeaninternal.server.deploy.parse.tenant;

public interface XEntityFinder {

  XEntity getEntity(Object tenantId,Class<?> beanClass);

  boolean isChanged(Object tenantId,Class<?> entityClass);

  <S> S getServiceObject(Class<S> clazz);
}
