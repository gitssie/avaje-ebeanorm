package io.ebeaninternal.server.deploy.parse;

import io.ebean.RawSql;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeaninternal.server.rawsql.SpiRawSql;

import java.util.LinkedList;
import java.util.List;

/**
 * Wraps information about a bean during deployment parsing.
 */
public final class DeployBeanInfo<T> {
  private final DeployBeanDescriptor<T> descriptor;
  private transient DeployUtil util;
  private transient DeployBeanPropertyAssoc<?> embeddedId;
  private transient XEntity entity;

  /**
   * Create with a DeployUtil and BeanDescriptor.
   */
  public DeployBeanInfo(DeployUtil util, DeployBeanDescriptor<T> descriptor) {
    this.util = util;
    this.descriptor = descriptor;
  }

  public DeployBeanInfo(DeployUtil util, DeployBeanDescriptor<T> descriptor, XEntity entity) {
    this.util = util;
    this.descriptor = descriptor;
    this.entity = entity;
  }

  @Override
  public String toString() {
    return String.valueOf(descriptor);
  }

  /**
   * Return the BeanDescriptor currently being processed.
   */
  public DeployBeanDescriptor<T> getDescriptor() {
    return descriptor;
  }

  /**
   * Return the DeployUtil we are using.
   */
  public DeployUtil getUtil() {
    return util;
  }

  /**
   * Add named RawSql from ebean.xml.
   */
  public void addRawSql(String name, RawSql rawSql) {
    descriptor.addRawSql(name, (SpiRawSql) rawSql);
  }

  /**
   * Add the named query.
   */
  public void addNamedQuery(String name, String query) {
    descriptor.addNamedQuery(name, query);
  }

  /**
   * Set that the PK is also a foreign key.
   */
  public void setPrimaryKeyJoin(TableJoin join) {
    descriptor.setPrimaryKeyJoin(join);
  }

  /**
   * This bean type has an embedded Id property.
   */
  public void setEmbeddedId(DeployBeanPropertyAssoc<?> embeddedId) {
    this.embeddedId = embeddedId;
  }

  public Class<?> getEmbeddedIdType() {
    return (embeddedId == null) ? null : embeddedId.getTargetType();
  }

  public boolean isEmbedded() {
    return descriptor.isEmbedded();
  }

  public XEntity getEntity() {
    return entity;
  }

  /**
   * 由于是延迟部署实体,所以需要保存部署对象的属性配置 DeployBeanDescriptor, 由于是常驻内存,需要清除掉一些一次性使用过后的引用
   */
  public void clear() {
    this.util = null;
    this.entity = null;
    this.embeddedId = null;
    List<DeployBeanProperty> properties = new LinkedList<>(descriptor.properties());
    for (DeployBeanProperty prop : properties) {
      if (prop instanceof DeployBeanPropertyAssoc<?> || prop.isId()) {
        continue;
      }
      descriptor.removeProperty(prop);
    }
  }
}
