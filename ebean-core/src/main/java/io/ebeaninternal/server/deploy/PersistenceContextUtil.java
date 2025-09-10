package io.ebeaninternal.server.deploy;

import jakarta.persistence.Entity;
import io.ebean.bean.DynamicEntity;

/**
 * Utility to find the root bean type.
 */
public final class PersistenceContextUtil {

  /**
   * Find and return the root bean type for the given class.
   */
  public static Class<?> root(Class<?> beanType) {
    if (DynamicEntity.class.isAssignableFrom(beanType)) {
      return beanType;
    }
    Class<?> parent = beanType.getSuperclass();
    while (parent != null && parent.isAnnotationPresent(Entity.class)) {
      beanType = parent;
      parent = parent.getSuperclass();
    }
    return beanType;
  }
}
