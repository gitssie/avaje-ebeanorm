package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.Transaction;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to generate values for a property rather than have then set by the user.
 * For example generate the update timestamp when a bean is updated.
 */
public interface GeneratedProperty {

  /**
   * Get the generated insert value for a specific property of a bean.
   */
  Object getInsertValue(BeanProperty prop, EntityBean bean, long now);

  default Object getInsertValue(BeanProperty prop, EntityBean bean, long now, Transaction transaction) {
    return getInsertValue(prop, bean, now);
  }

  /**
   * Get the generated update value for a specific property of a bean.
   */
  Object getUpdateValue(BeanProperty prop, EntityBean bean, long now);

  default Object getUpdateValue(BeanProperty prop, EntityBean bean, long now, Transaction transaction) {
    return getUpdateValue(prop, bean, now);
  }

  default void deleteRecurse(BeanProperty prop, EntityBean bean, Transaction transaction) {
    //passed
  }

  /**
   * Return true if this should always be includes in an update statement.
   * <p>
   * Used to include GeneratedUpdateTimestamp in dynamic table updates.
   * </p>
   */
  boolean includeInUpdate();

  /**
   * Return true if the property should be included in an update even if
   * it is not loaded (ie. Last Updated Timestamp).
   */
  boolean includeInAllUpdates();

  /**
   * Return true if this should be included in insert statements.
   */
  boolean includeInInsert();

  /**
   * Return true if this should be included in delete statements.
   */
  default boolean includeInDelete() {
    return false;
  }

  /**
   * Return true if the GeneratedProperty implies the DDL to create the DB
   * column should have a not null constraint.
   */
  boolean isDDLNotNullable();

}
