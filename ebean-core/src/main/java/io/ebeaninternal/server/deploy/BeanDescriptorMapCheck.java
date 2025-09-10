package io.ebeaninternal.server.deploy;

import io.ebean.annotation.ConstraintMode;
import io.ebean.bean.BeanCollection;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.deploy.meta.*;
import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;
import io.ebeaninternal.server.type.TypeManager;

import jakarta.persistence.PersistenceException;
import java.util.*;

import static java.lang.System.Logger.Level.*;

//copy from BeanDescriptorManager
public class BeanDescriptorMapCheck {
  protected static final System.Logger log = CoreLog.internal;
  protected TypeManager typeManager;
  protected BeanDescriptorMapTemporal descriptorMap;

  public BeanDescriptorMapCheck(TypeManager typeManager, BeanDescriptorMapTemporal descriptorMap) {
    this.typeManager = typeManager;
    this.descriptorMap = descriptorMap;
  }

  /**
   * Sets the inheritance info.
   */
  protected void setInheritanceInfo(DeployBeanInfo<?> info) {
    for (DeployBeanPropertyAssocOne<?> oneProp : info.getDescriptor().propertiesAssocOne()) {
      if (!oneProp.isTransient()) {
        DeployBeanInfo<?> assoc = descriptorMap.descInfo(oneProp.getTargetType());
        if (assoc != null) {
          oneProp.getTableJoin().setInheritInfo(assoc.getDescriptor().getInheritInfo());
        }
      }
    }
    for (DeployBeanPropertyAssocMany<?> manyProp : info.getDescriptor().propertiesAssocMany()) {
      if (!manyProp.isTransient()) {
        DeployBeanInfo<?> assoc = descriptorMap.descInfo(manyProp.getTargetType());
        if (assoc != null) {
          manyProp.getTableJoin().setInheritInfo(assoc.getDescriptor().getInheritInfo());
        }
      }
    }
  }

  protected void secondaryPropsJoins(DeployBeanInfo<?> info) {
    DeployBeanDescriptor<?> descriptor = info.getDescriptor();
    for (DeployBeanProperty prop : descriptor.propertiesBase()) {
      if (prop.isSecondaryTable()) {
        String tableName = prop.getSecondaryTable();
        // find a join to that table...
        DeployBeanPropertyAssocOne<?> assocOne = descriptor.findJoinToTable(tableName);
        if (assocOne == null) {
          String msg = "Error with property " + prop + ". Could not find a Relationship to table " + tableName
            + ". Perhaps you could use a @JoinColumn instead.";
          throw new RuntimeException(msg);
        }
        DeployTableJoin tableJoin = assocOne.getTableJoin();
        prop.setSecondaryTableJoin(tableJoin, assocOne.getName());
      }
    }
  }

  /**
   * Check the mappedBy attributes for properties on this descriptor.
   * <p>
   * This will read join information defined on the 'owning/other' side of the
   * relationship. It also does some extra work for unidirectional
   * relationships.
   */
  protected void checkMappedBy(DeployBeanInfo<?> info, List<DeployBeanPropertyAssocOne<?>> primaryKeyJoinCheck) {
    for (DeployBeanPropertyAssocOne<?> oneProp : info.getDescriptor().propertiesAssocOne()) {
      if (!oneProp.isTransient()) {
        if (oneProp.getMappedBy() != null) {
          checkMappedByOneToOne(oneProp);
        } else if (oneProp.isPrimaryKeyJoin()) {
          primaryKeyJoinCheck.add(oneProp);
        }
      }
    }

    for (DeployBeanPropertyAssocMany<?> manyProp : info.getDescriptor().propertiesAssocMany()) {
      if (!manyProp.isTransient()) {
        if (manyProp.isManyToMany()) {
          checkMappedByManyToMany(manyProp);
        } else {
          checkMappedByOneToMany(info, manyProp);
        }
      }
    }
  }

  protected DeployBeanDescriptor<?> targetDescriptor(DeployBeanPropertyAssoc<?> prop) {
    Class<?> targetType = prop.getTargetType();
    DeployBeanInfo<?> info = descriptorMap.descInfo(targetType);
    if (info == null) {
      throw new PersistenceException("Can not find descriptor [" + targetType + "] for " + prop);
    }
    return info.getDescriptor();
  }

  /**
   * Check that the many property has either an implied mappedBy property or
   * mark it as unidirectional.
   */
  protected boolean findMappedBy(DeployBeanPropertyAssocMany<?> prop) {
    // this is the entity bean type - that owns this property
    Class<?> owningType = prop.getOwningType();
    Set<String> matchSet = new HashSet<>();

    // get the bean descriptor that holds the mappedBy property
    DeployBeanDescriptor<?> targetDesc = targetDescriptor(prop);
    List<DeployBeanPropertyAssocOne<?>> ones = targetDesc.propertiesAssocOne();
    for (DeployBeanPropertyAssocOne<?> possibleMappedBy : ones) {
      Class<?> possibleMappedByType = possibleMappedBy.getTargetType();
      if (possibleMappedByType.equals(owningType)) {
        prop.setMappedBy(possibleMappedBy.getName());
        matchSet.add(possibleMappedBy.getName());
      }
    }

    if (matchSet.isEmpty()) {
      // this is a unidirectional relationship
      // ... that is no matching property on the 'detail' bean
      return false;
    }
    if (matchSet.size() == 1) {
      // all right with the world
      prop.clearTableJoin();
      return true;
    }
    if (matchSet.size() == 2) {
      // try to find a match implicitly using a common naming convention
      // e.g. List<Bug> loggedBugs; ... search for "logged" in matchSet
      String name = prop.getName();

      // get the target type short name
      String targetType = prop.getTargetType().getName();
      String shortTypeName = targetType.substring(targetType.lastIndexOf('.') + 1);

      // name includes (probably ends with) the target type short name?
      int p = name.indexOf(shortTypeName);
      if (p > 1) {
        // ok, get the 'interesting' part of the property name
        // That is the name without the target type
        String searchName = name.substring(0, p).toLowerCase();

        // search for this in the possible matches
        for (String possibleMappedBy : matchSet) {
          String possibleLower = possibleMappedBy.toLowerCase();
          if (possibleLower.contains(searchName)) {
            // we have a match..
            prop.setMappedBy(possibleMappedBy);

            String m = "Implicitly found mappedBy for " + targetDesc + "." + prop;
            m += " by searching for [" + searchName + "] against " + matchSet;
            log.log(DEBUG,m);

            return true;
          }
        }
      }
    }
    // multiple options so should specify mappedBy property
    String msg = "Error on " + prop + " missing mappedBy.";
    msg += " There are [" + matchSet.size() + "] possible properties in " + targetDesc;
    msg += " that this association could be mapped to. Please specify one using ";
    msg += "the mappedBy attribute on @OneToMany.";
    throw new PersistenceException(msg);
  }

  protected void makeOrderColumn(DeployBeanPropertyAssocMany<?> oneToMany) {
    DeployBeanDescriptor<?> targetDesc = targetDescriptor(oneToMany);
    DeployOrderColumn orderColumn = oneToMany.getOrderColumn();
    final ScalarType<?> scalarType = typeManager.type(Integer.class);
    DeployBeanProperty orderProperty = new DeployBeanProperty(targetDesc, Integer.class, scalarType, null);
    orderProperty.setName(DeployOrderColumn.LOGICAL_NAME);
    orderProperty.setDbColumn(orderColumn.getName());
    orderProperty.setNullable(orderColumn.isNullable());
    orderProperty.setDbInsertable(orderColumn.isInsertable());
    orderProperty.setDbUpdateable(orderColumn.isUpdatable());
    orderProperty.setDbRead(true);
    orderProperty.setOwningType(targetDesc.getBeanType());
    final InheritInfo targetInheritInfo = targetDesc.getInheritInfo();
    if (targetInheritInfo != null) {
      for (InheritInfo child : targetInheritInfo.getChildren()) {
        final DeployBeanDescriptor<?> childDescriptor = descriptorMap.descInfo(child.getType()).getDescriptor();
        childDescriptor.setOrderColumn(orderProperty);
      }
    }
    targetDesc.setOrderColumn(orderProperty);
  }

  /**
   * A OneToMany with no matching mappedBy property in the target so must be
   * unidirectional.
   * <p>
   * This means that inserts MUST cascade for this property.
   * <p>
   * Create a "Shadow"/Unidirectional property on the target. It is used with
   * inserts to set the foreign key value (e.g. inserts the foreign key value
   * into the order_id column on the order_lines table).
   */
  protected void makeUnidirectional(DeployBeanPropertyAssocMany<?> oneToMany) {
    DeployBeanDescriptor<?> targetDesc = targetDescriptor(oneToMany);
    Class<?> owningType = oneToMany.getOwningType();
    if (!oneToMany.getCascadeInfo().isSave()) {
      // The property MUST have persist cascading so that inserts work.
      Class<?> targetType = oneToMany.getTargetType();
      String msg = "Error on " + oneToMany + ". @OneToMany MUST have ";
      msg += "Cascade.PERSIST or Cascade.ALL because this is a unidirectional ";
      msg += "relationship. That is, there is no property of type " + owningType + " on " + targetType;
      throw new PersistenceException(msg);
    }

    // mark this property as unidirectional
    oneToMany.setUnidirectional();
    // specify table and table alias...
    BeanTable beanTable = beanTable(owningType);
    // define the TableJoin
    DeployTableJoin oneToManyJoin = oneToMany.getTableJoin();
    if (!oneToManyJoin.hasJoinColumns()) {
      throw new RuntimeException("No join columns");
    }
    createUnidirectional(targetDesc, owningType, beanTable, oneToManyJoin);
  }

  /**
   * Create and add a Unidirectional property (for ElementCollection) which maps to the foreign key.
   */
  public <A> void createUnidirectional(DeployBeanDescriptor<?> targetDesc, Class<A> targetType, BeanTable beanTable, DeployTableJoin oneToManyJoin) {

    // create the 'shadow' unidirectional property
    // which is put on the target descriptor
    DeployBeanPropertyAssocOne<A> unidirectional = new DeployBeanPropertyAssocOne<>(targetDesc, targetType);
    unidirectional.setUndirectionalShadow();
    unidirectional.setNullable(false);
    unidirectional.setDbRead(true);
    unidirectional.setDbInsertable(true);
    unidirectional.setDbUpdateable(false);
    unidirectional.setBeanTable(beanTable);
    unidirectional.setName(beanTable.getBaseTable());
    unidirectional.setJoinType(true);
    unidirectional.setJoinColumns(oneToManyJoin.columns(), true);

    targetDesc.setUnidirectional(unidirectional);
  }

  protected void checkMappedByOneToOne(DeployBeanPropertyAssocOne<?> prop) {
    // check that the mappedBy property is valid and read
    // its associated join information if it is available
    String mappedBy = prop.getMappedBy();
    // get the mappedBy property
    DeployBeanDescriptor<?> targetDesc = targetDescriptor(prop);
    DeployBeanPropertyAssocOne<?> mappedAssocOne = mappedOneToOne(prop, mappedBy, targetDesc);
    DeployTableJoin tableJoin = prop.getTableJoin();
    if (!tableJoin.hasJoinColumns()) {
      // define Join as the inverse of the mappedBy property
      DeployTableJoin otherTableJoin = mappedAssocOne.getTableJoin();
      otherTableJoin.copyWithoutType(tableJoin, true, tableJoin.getTable());
    }

    if (mappedAssocOne.isPrimaryKeyJoin()) {
      // bi-directional PrimaryKeyJoin ...
      mappedAssocOne.setPrimaryKeyJoin(false);
      prop.setPrimaryKeyExport();
      addPrimaryKeyJoin(prop);
    }
  }

  protected DeployBeanPropertyAssocOne<?> mappedOneToOne(DeployBeanPropertyAssocOne<?> prop, String mappedBy, DeployBeanDescriptor<?> targetDesc) {
    DeployBeanProperty mappedProp = targetDesc.getBeanProperty(mappedBy);
    if (mappedProp == null) {
      throw new PersistenceException("Error on " + prop + " Can not find mappedBy property [" + targetDesc + "." + mappedBy + "]");
    }
    if (!(mappedProp instanceof DeployBeanPropertyAssocOne<?>)) {
      throw new PersistenceException("Error on " + prop + ". mappedBy property [" + targetDesc + "." + mappedBy + "]is not a OneToOne?");
    }
    DeployBeanPropertyAssocOne<?> mappedAssocOne = (DeployBeanPropertyAssocOne<?>) mappedProp;
    if (!mappedAssocOne.isOneToOne()) {
      throw new PersistenceException("Error on " + prop + ". mappedBy property [" + targetDesc + "." + mappedBy + "]is not a OneToOne?");
    }
    return mappedAssocOne;
  }

  protected void checkUniDirectionalPrimaryKeyJoin(DeployBeanPropertyAssocOne<?> prop) {
    if (prop.isPrimaryKeyJoin()) {
      // uni-directional PrimaryKeyJoin ...
      prop.setPrimaryKeyExport();
      addPrimaryKeyJoin(prop);
    }
  }

  /**
   * If the property has mappedBy set then do two things. Make sure the mappedBy
   * property exists, and secondly read its join information.
   * <p>
   * We can use the join information from the mappedBy property and reverse it
   * for using in the OneToMany direction.
   */
  protected void checkMappedByOneToMany(DeployBeanInfo<?> info, DeployBeanPropertyAssocMany<?> prop) {
    if (prop.isElementCollection()) {
      // skip mapping check
      return;
    }
    DeployBeanDescriptor<?> targetDesc = targetDescriptor(prop);
    if (targetDesc.isDraftableElement()) {
      // automatically turning on orphan removal and CascadeType.ALL
      prop.setModifyListenMode(BeanCollection.ModifyListenMode.REMOVALS);
      prop.getCascadeInfo().setSaveDelete(true, true);
    }

    if (prop.hasOrderColumn()) {
      makeOrderColumn(prop);
    }

    if (prop.getMappedBy() == null) {
      // if we are doc store only we are done
      // this allows the use of @OneToMany in @DocStore - Entities
      if (info.getDescriptor().isDocStoreOnly()) {
        prop.setUnidirectional();
        return;
      }

      if (!findMappedBy(prop)) {
        if (!prop.isO2mJoinTable()) {
          makeUnidirectional(prop);
        }
        return;
      }
    }

    // check that the mappedBy property is valid and read
    // its associated join information if it is available
    String mappedBy = prop.getMappedBy();

    // get the mappedBy property
    DeployBeanPropertyAssocOne<?> mappedAssocOne = mappedManyToOne(prop, targetDesc, mappedBy);
    DeployTableJoin tableJoin = prop.getTableJoin();
    if (!tableJoin.hasJoinColumns()) {
      // define Join as the inverse of the mappedBy property
      DeployTableJoin otherTableJoin = mappedAssocOne.getTableJoin();
      otherTableJoin.copyTo(tableJoin, true, tableJoin.getTable());
    }

    PropertyForeignKey foreignKey = mappedAssocOne.getForeignKey();
    if (foreignKey != null) {
      ConstraintMode onDelete = foreignKey.getOnDelete();
      switch (onDelete) {
        case SET_DEFAULT:
        case SET_NULL:
        case CASCADE: {
          // turn off cascade delete when we are using the foreign
          // key constraint to cascade the delete or set null
          prop.getCascadeInfo().setDelete(false);
        }
      }
    }
  }

  protected DeployBeanPropertyAssocOne<?> mappedManyToOne(DeployBeanPropertyAssocMany<?> prop, DeployBeanDescriptor<?> targetDesc, String mappedBy) {
    DeployBeanProperty mappedProp = targetDesc.getBeanProperty(mappedBy);
    if (mappedProp == null) {
      throw new PersistenceException("Error on " + prop + "  Can not find mappedBy property [" + mappedBy + "] " + "in [" + targetDesc + "]");
    }
    if (!(mappedProp instanceof DeployBeanPropertyAssocOne<?>)) {
      throw new PersistenceException("Error on " + prop + ". mappedBy property [" + mappedBy + "]is not a ManyToOne?" + "in [" + targetDesc + "]");
    }
    return (DeployBeanPropertyAssocOne<?>) mappedProp;
  }

  /**
   * For mappedBy copy the joins from the other side.
   */
  protected void checkMappedByManyToMany(DeployBeanPropertyAssocMany<?> prop) {
    // get the bean descriptor that holds the mappedBy property
    String mappedBy = prop.getMappedBy();
    if (mappedBy == null) {
      if (targetDescriptor(prop).isDraftable()) {
        prop.setIntersectionDraftTable();
      }
      return;
    }

    // get the mappedBy property
    DeployBeanDescriptor<?> targetDesc = targetDescriptor(prop);
    DeployBeanPropertyAssocMany<?> mappedAssocMany = mappedManyToMany(prop, mappedBy, targetDesc);

    // define the relationships/joins on this side as the
    // reverse of the other mappedBy side ...
    DeployTableJoin mappedIntJoin = mappedAssocMany.getIntersectionJoin();
    DeployTableJoin mappendInverseJoin = mappedAssocMany.getInverseJoin();

    String intTableName = mappedIntJoin.getTable();

    DeployTableJoin tableJoin = prop.getTableJoin();
    mappedIntJoin.copyTo(tableJoin, true, targetDesc.getBaseTable());

    DeployTableJoin intJoin = new DeployTableJoin();
    mappendInverseJoin.copyTo(intJoin, false, intTableName);
    prop.setIntersectionJoin(intJoin);

    DeployTableJoin inverseJoin = new DeployTableJoin();
    mappedIntJoin.copyTo(inverseJoin, false, intTableName);
    prop.setInverseJoin(inverseJoin);

    if (targetDesc.isDraftable()) {
      prop.setIntersectionDraftTable();
    }
  }

  protected DeployBeanPropertyAssocMany<?> mappedManyToMany(DeployBeanPropertyAssocMany<?> prop, String mappedBy, DeployBeanDescriptor<?> targetDesc) {
    DeployBeanProperty mappedProp = targetDesc.getBeanProperty(mappedBy);
    if (mappedProp == null) {
      throw new PersistenceException("Error on " + prop + "  Can not find mappedBy property [" + mappedBy + "] " + "in [" + targetDesc + "]");
    }
    if (!(mappedProp instanceof DeployBeanPropertyAssocMany<?>)) {
      throw new PersistenceException("Error on " + prop + ". mappedBy property [" + targetDesc + "." + mappedBy + "] is not a ManyToMany?");
    }

    DeployBeanPropertyAssocMany<?> mappedAssocMany = (DeployBeanPropertyAssocMany<?>) mappedProp;
    if (!mappedAssocMany.isManyToMany()) {
      throw new PersistenceException("Error on " + prop + ". mappedBy property [" + targetDesc + "." + mappedBy + "] is not a ManyToMany?");
    }
    return mappedAssocMany;
  }

  protected void addPrimaryKeyJoin(DeployBeanPropertyAssocOne<?> prop) {
    String baseTable = prop.getDesc().getBaseTable();
    DeployTableJoin inverse = prop.getTableJoin().createInverse(baseTable);
    TableJoin inverseJoin = new TableJoin(inverse, prop.getForeignKey());
    DeployBeanInfo<?> target = descriptorMap.descInfo(prop.getTargetType());
    target.setPrimaryKeyJoin(inverseJoin);
  }

  public BeanTable beanTable(Class<?> type) {
    return descriptorMap.beanTable(type);
  }

}
