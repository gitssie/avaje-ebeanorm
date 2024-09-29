package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.DbForeignKey;
import io.ebean.annotation.FetchPreference;
import io.ebean.annotation.TenantId;
import io.ebean.annotation.Where;
import io.ebean.config.NamingConvention;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.deploy.BeanDescriptorMap;
import io.ebeaninternal.server.deploy.BeanTable;
import io.ebeaninternal.server.deploy.PropertyForeignKey;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;
import io.ebeaninternal.server.deploy.parse.tenant.XField;
import io.ebeaninternal.server.query.SqlJoinType;

import javax.persistence.*;

import static java.lang.System.Logger.Level.INFO;

/**
 * Read the deployment annotations for Associated One beans.
 */
final class XAnnotationAssocOnes extends XAnnotationAssoc {

  /**
   * Create with the deploy Info.
   */
  XAnnotationAssocOnes(DeployBeanInfo<?> info, ReadAnnotationConfig readConfig, BeanDescriptorMap factory) {
    super(info, readConfig, factory);
  }

  /**
   * Parse the annotation.
   */
  @Override
  public void parse() {
    if (info.getEntity() == null) {
      return;
    }
    for (DeployBeanProperty prop : descriptor.propertiesAll()) {
      if (prop instanceof DeployBeanPropertyAssocOne<?>) {
        if (prop.getField() == null) {
          XField field = info.getEntity().getField(prop.getName());
          readAssocOne(field, (DeployBeanPropertyAssocOne<?>) prop);
        }
      }
    }
  }

  private void readAssocOne(XField field, DeployBeanPropertyAssocOne<?> prop) {
    ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
    if (manyToOne != null) {
      readManyToOne(manyToOne, prop);
      if (field.getAnnotation(TenantId.class) != null) {
        prop.setTenantId();
      }
    }
    OneToOne oneToOne = field.getAnnotation(OneToOne.class);
    if (oneToOne != null) {
      readOneToOne(oneToOne, prop);
    }
    Embedded embedded = field.getAnnotation(Embedded.class);
    if (embedded != null) {
      readEmbedded(prop, embedded);
    }
    EmbeddedId emId = field.getAnnotation(EmbeddedId.class);
    if (emId != null) {
      prop.setEmbedded();
      prop.setId();
      prop.setNullable(false);
    }
    Column column = field.getAnnotation(Column.class);
    if (column != null && !isEmpty(column.name())) {
      // have this in for AssocOnes used on
      // Sql based beans...
      prop.setDbColumn(column.name());
    }

    // May as well check for Id. Makes sense to me.
    Id id = field.getAnnotation(Id.class);
    if (id != null) {
      readIdAssocOne(prop);
    }

    DbForeignKey dbForeignKey = field.getAnnotation(DbForeignKey.class);
    if (dbForeignKey != null) {
      prop.setForeignKey(new PropertyForeignKey(dbForeignKey));
    }

    Where where = getMetaAnnotationWhere(field, platform);
    if (where != null) {
      // not expecting this to be used on assoc one properties
      prop.setExtraWhere(processFormula(where.clause()));
    }

    PrimaryKeyJoinColumn primaryKeyJoin = field.getAnnotation(PrimaryKeyJoinColumn.class);
    if (primaryKeyJoin != null) {
      readPrimaryKeyJoin(primaryKeyJoin, prop);
    }

    FetchPreference fetchPreference = field.getAnnotation(FetchPreference.class);
    if (fetchPreference != null) {
      prop.setFetchPreference(fetchPreference.value());
    }

    io.ebean.annotation.NotNull nonNull = field.getAnnotation(io.ebean.annotation.NotNull.class);
    if (nonNull != null) {
      prop.setNullable(false);
    }
    if (!field.isNullable()) {
      // overrides optional attribute of ManyToOne etc
      prop.setNullable(false);
      prop.getTableJoin().setType(SqlJoinType.INNER);
    }

    // check for manually defined joins
    BeanTable beanTable = prop.getBeanTable();
    JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
    if (joinColumn != null) {
      setFromJoinColumn(prop, beanTable, joinColumn);
      checkForNoConstraint(prop, joinColumn);
    }

    JoinTable joinTable = field.getAnnotation(JoinTable.class);
    if (joinTable != null) {
      for (JoinColumn joinCol : joinTable.joinColumns()) {
        setFromJoinColumn(prop, beanTable, joinCol);
      }
    }

    prop.setJoinType(prop.isNullable());

    if (!prop.getTableJoin().hasJoinColumns() && beanTable != null) {

      //noinspection StatementWithEmptyBody
      if (prop.getMappedBy() != null) {
        // the join is derived by reversing the join information
        // from the mapped by property.
        // Refer BeanDescriptorManager.readEntityRelationships()

      } else {
        // use naming convention to define join.
        NamingConvention nc = factory.namingConvention();

        String fkeyPrefix = null;
        if (nc.isUseForeignKeyPrefix()) {
          fkeyPrefix = nc.getColumnFromProperty(beanType, prop.getName());
        }

        beanTable.createJoinColumn(fkeyPrefix, prop.getTableJoin(), true, prop.getSqlFormulaSelect());
      }
    }
  }

  private void setFromJoinColumn(DeployBeanPropertyAssocOne<?> prop, BeanTable beanTable, JoinColumn joinColumn) {
    if (beanTable == null) {
      throw new IllegalStateException("Looks like a missing @ManyToOne or @OneToOne on property " + prop.getFullBeanName() + " - no related 'BeanTable'");
    }
    prop.getTableJoin().addJoinColumn(util, false, joinColumn, beanTable);
    if (!joinColumn.updatable()) {
      prop.setDbUpdateable(false);
    }
    if (!joinColumn.nullable()) {
      prop.setNullable(false);
    }
  }

  private void checkForNoConstraint(DeployBeanPropertyAssocOne<?> prop, JoinColumn joinColumn) {
    try {
      ForeignKey foreignKey = joinColumn.foreignKey();
      if (foreignKey.value() == ConstraintMode.NO_CONSTRAINT) {
        prop.setForeignKey(new PropertyForeignKey());
      }
    } catch (NoSuchMethodError e) {
      // support old JPA API
    }
  }

  private void readManyToOne(ManyToOne propAnn, DeployBeanPropertyAssocOne<?> beanProp) {
    setCascadeTypes(propAnn.cascade(), beanProp.getCascadeInfo());
    setTargetType(propAnn.targetEntity(), beanProp);
    setBeanTable(beanProp);
    beanProp.setDbInsertable(true);
    beanProp.setDbUpdateable(true);
    beanProp.setNullable(propAnn.optional());
    beanProp.setFetchType(propAnn.fetch());
  }

  private void readOneToOne(OneToOne propAnn, DeployBeanPropertyAssocOne<?> prop) {
    prop.setOneToOne();
    prop.setDbInsertable(true);
    prop.setDbUpdateable(true);
    prop.setNullable(propAnn.optional());
    prop.setFetchType(propAnn.fetch());
    prop.setMappedBy(propAnn.mappedBy());
    if (readOrphanRemoval(propAnn)) {
      prop.setOrphanRemoval();
    }
    if (!"".equals(propAnn.mappedBy())) {
      prop.setOneToOneExported();
    }

    setCascadeTypes(propAnn.cascade(), prop.getCascadeInfo());
    setTargetType(propAnn.targetEntity(), prop);
    setBeanTable(prop);
  }

  private boolean readOrphanRemoval(OneToOne property) {
    try {
      return property.orphanRemoval();
    } catch (NoSuchMethodError e) {
      // Support old JPA API
      return false;
    }
  }

  private void readPrimaryKeyJoin(PrimaryKeyJoinColumn primaryKeyJoin, DeployBeanPropertyAssocOne<?> prop) {
    if (!prop.isOneToOne()) {
      throw new IllegalStateException("Expecting property " + prop.getFullBeanName() + " with PrimaryKeyJoinColumn to be a OneToOne?");
    }
    prop.setPrimaryKeyJoin(true);

    if (!primaryKeyJoin.name().isEmpty()) {
      CoreLog.internal.log(INFO, "Automatically determining join columns for @PrimaryKeyJoinColumn - ignoring PrimaryKeyJoinColumn.name attribute [{}] on {}", primaryKeyJoin.name(), prop.getFullBeanName());
    }
    if (!primaryKeyJoin.referencedColumnName().isEmpty()) {
      CoreLog.internal.log(INFO, "Automatically determining join columns for @PrimaryKeyJoinColumn - Ignoring PrimaryKeyJoinColumn.referencedColumnName attribute [{}] on {}", primaryKeyJoin.referencedColumnName(), prop.getFullBeanName());
    }
    BeanTable baseBeanTable = factory.beanTable(info.getDescriptor().getBeanType());
    String localPrimaryKey = baseBeanTable.getIdColumn();
    String foreignColumn = getBeanTable(prop).getIdColumn();
    prop.getTableJoin().addJoinColumn(new DeployTableJoinColumn(localPrimaryKey, foreignColumn, false, false));
  }

  private void readEmbedded(DeployBeanPropertyAssocOne<?> prop, Embedded embedded) {
    if (descriptor.isDocStoreOnly() && prop.getDocStoreDoc() == null) {
      prop.setDocStoreEmbedded("");
    }
    prop.setEmbedded();
    prop.setDbInsertable(true);
    prop.setDbUpdateable(true);
    try {
      prop.setColumnPrefix(embedded.prefix());
    } catch (NoSuchMethodError e) {
      // using standard JPA API without prefix option, maybe in EE container
    }
    readEmbeddedAttributeOverrides(prop);
  }
}
