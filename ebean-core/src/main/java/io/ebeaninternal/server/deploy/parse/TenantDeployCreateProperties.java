package io.ebeaninternal.server.deploy.parse;

import io.ebean.Model;
import io.ebean.annotation.*;
import io.ebean.core.type.ScalarType;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.deploy.ManyType;
import io.ebeaninternal.server.deploy.meta.*;
import io.ebeaninternal.server.deploy.parse.DetermineManyType;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeaninternal.server.deploy.parse.tenant.XEntityProvider;
import io.ebeaninternal.server.deploy.parse.tenant.XField;
import io.ebeaninternal.server.type.TypeManager;

import javax.persistence.PersistenceException;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

public class TenantDeployCreateProperties extends DeployCreateProperties{
  private XEntityProvider entityProvider;

  public TenantDeployCreateProperties(TypeManager typeManager) {
    super(typeManager);
    entityProvider = new XEntityProvider();
  }

  private void setProperties(DeployBeanDescriptor<?> desc) {
    String[] properties = new String[desc.properties().size()];
    int i=0;
    for (DeployBeanProperty property : desc.properties()) {
      properties[i++] = property.getName();
    }
    desc.setProperties(properties);
  }

  protected void createProperties(DeployBeanDescriptor<?> desc,XEntity entity, Class<?> beanType) {
    if (beanType.equals(Model.class)) {
      // ignore all fields on model (_$dbName)
      return;
    }
    try {
      Collection<XField> fields = entity.getFields();
      int i=-1;
      for (XField field : fields) {
        i++;
        if (!ignoreFieldByName(field.getName())) {
          DeployBeanProperty prop = createProp(desc, field, beanType);
          if (prop != null) {
            // set a order that gives priority to inherited properties
            // push Id/EmbeddedId up and CreatedTimestamp/UpdatedTimestamp down
            int sortOverride = prop.getSortOverride();
            prop.setSortOrder((1 * 10000 + 100 - i + sortOverride));

            DeployBeanProperty replaced = desc.addBeanProperty(prop);
            if (replaced != null && !replaced.isTransient()) {
              String msg = "Huh??? property " + prop.getFullBeanName() + " being defined twice";
              msg += " but replaced property was not transient? This is not expected?";
              CoreLog.log.warn(msg);
            }
          }
        }
      }
    } catch (PersistenceException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new PersistenceException(ex);
    }
  }

  private DeployBeanProperty createProp(DeployBeanDescriptor<?> desc, XField field, Class<?> beanType) {
    DeployBeanProperty prop = createProp(desc, field);
    if (prop == null) {
      // transient annotation on unsupported type
      return null;
    } else {
      prop.setOwningType(beanType);
      prop.setName(field.getName());
      prop.setField(null);
      return prop;
    }
  }
  private DeployBeanProperty createProp(DeployBeanDescriptor<?> desc, XField field) {
    Class<?> propertyType = field.getType();
    if (isSpecialScalarType(field)) {
      return new DeployBeanProperty(desc, propertyType, field.getType());
    }
    // check for Collection type (list, set or map)
    ManyType manyType = determineManyType.getManyType(propertyType);
    if (manyType != null) {
      // List, Set or Map based object
      Class<?> targetType = determineTargetType(field);
      if (targetType == null) {
        if (field.has(Transient.class)) {
          // not supporting this field (generic type used)
          return null;
        }
        CoreLog.internal.warn("Could not find parameter type (via reflection) on " + desc.getFullName() + " " + field.getName());
      }
      return createManyType(desc, targetType, manyType);
    }
    if (propertyType.isEnum() || propertyType.isPrimitive()) {
      return new DeployBeanProperty(desc, propertyType, null, null);
    }
    ScalarType<?> scalarType = typeManager.getScalarType(propertyType);
    if (scalarType != null) {
      return new DeployBeanProperty(desc, propertyType, scalarType, null);
    }
    if (isTransientField(field)) {
      // return with no ScalarType (still support JSON features)
      return new DeployBeanProperty(desc, propertyType, null, null);
    }
    try {
      return new DeployBeanPropertyAssocOne(desc, propertyType);

    } catch (Exception e) {
      CoreLog.log.error("Error with " + desc + " field:" + field.getName(), e);
      return null;
    }
  }

  private boolean isSpecialScalarType(XField field) {
    return (field.has(DbJson.class))
      || (field.has(DbJsonB.class))
      || (field.has(DbArray.class))
      || (field.has(DbMap.class))
      || (field.has(UnmappedJson.class));
  }

  private boolean isTransientField(XField field) {
    //return AnnotationUtil.has(field, Transient.class);
    return false;
  }

  private Class<?> determineTargetType(XField field) {
    return field.getType();
  }

  private DeployBeanProperty createManyType(DeployBeanDescriptor<?> desc, Class<?> targetType, ManyType manyType) {
    try {
      ScalarType<?> scalarType = typeManager.getScalarType(targetType);
      if (scalarType != null) {
        return new DeployBeanPropertySimpleCollection(desc, targetType, manyType);
      }
    } catch (NullPointerException e) {
      CoreLog.internal.debug("expected non-scalar type {}", e.getMessage());
    }
    return new DeployBeanPropertyAssocMany(desc, targetType, manyType);
  }


  /**
   * 创建Deploy Bean
   * @param deployUtil
   * @param desc
   * @param readAnnotations
   * @return
   * @param <T>
   */
  public <T> DeployBeanInfo<T> createDeployBeanInfo(DeployUtil deployUtil, DeployBeanDescriptor<T> desc, XReadAnnotations readAnnotations) {
    XEntity entity = entityProvider.getEntity(desc);
    createProperties(desc, entity,desc.getBeanType());
    desc.sortProperties();
    setProperties(desc);

    DeployBeanInfo<T> info = new DeployBeanInfo<>(deployUtil, desc);
    readAnnotations.readInitial(entity,info); //初始化属性
    return info;
  }
}
