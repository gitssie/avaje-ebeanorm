package io.ebeaninternal.server.deploy.parse.tenant;

public interface XEntityFinder {

  XEntity getEntity(Class<?> beanClass);

  boolean isChanged(Class<?> entityClass);
}
