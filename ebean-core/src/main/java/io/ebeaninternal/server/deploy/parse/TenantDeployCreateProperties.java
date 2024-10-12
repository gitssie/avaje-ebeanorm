package io.ebeaninternal.server.deploy.parse;

import io.ebean.Model;
import io.ebean.annotation.*;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.deploy.ManyType;
import io.ebeaninternal.server.deploy.meta.*;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeaninternal.server.deploy.parse.tenant.XEntityFinder;
import io.ebeaninternal.server.deploy.parse.tenant.XField;
import io.ebeaninternal.server.deploy.parse.tenant.annotation.GenericType;
import io.ebeaninternal.server.deploy.parse.tenant.annotation.XGenericType;
import io.ebeaninternal.server.type.TypeManager;

import javax.persistence.PersistenceException;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;

import static java.lang.System.Logger.Level.*;

public class TenantDeployCreateProperties {
  private XEntityFinder entityProvider;
  protected final DetermineManyType determineManyType;
  protected final TypeManager typeManager;

  public TenantDeployCreateProperties(DeployCreateProperties createProperties, XEntityFinder entityProvider) {
    this.typeManager = createProperties.typeManager;
    this.determineManyType = createProperties.determineManyType;
    this.entityProvider = entityProvider;
  }

  private void setProperties(DeployBeanDescriptor<?> desc) {
    String[] properties = new String[desc.properties().size()];
    int i = 0;
    for (DeployBeanProperty property : desc.properties()) {
      properties[i++] = property.getName();
    }
    desc.setProperties(properties);
  }

  protected void changeBeanProperty(DeployBeanDescriptor<?> desc, DeployBeanProperty property, XField field, Class<?> beanType) throws Exception {
    if (!field.getAnnotations().isEmpty()) {
      if (property.getClass().equals(DeployBeanProperty.class)) {
        DeployBeanProperty prop = new DeployBeanProperty(desc, property.getPropertyType(), property.getGenericType());
        Field[] fields = DeployBeanProperty.class.getDeclaredFields();
        for (Field rField : fields) {
          rField.setAccessible(true);
          if (Modifier.isStatic(rField.getModifiers())) {
            continue;
          }
          rField.set(prop, rField.get(property));
        }
        prop.setOwningType(beanType);
        desc.addBeanProperty(prop);
      }
    }
  }

  protected void createProperties(DeployBeanDescriptor<?> desc, XEntity entity, Class<?> beanType) {
    if (beanType.equals(Model.class)) {
      // ignore all fields on model (_$dbName)
      return;
    }
    try {
      int i = 0;
      for (XField field : entity.getFields()) {
        DeployBeanProperty prop = desc.getBeanProperty(field.getName());
        if (prop != null) {
          continue;
        }
        addProperty(i++, desc, createProp(desc, field, beanType));
      }
    } catch (PersistenceException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new PersistenceException(ex);
    }
  }

  private void addProperty(int index, DeployBeanDescriptor<?> desc, DeployBeanProperty prop) {
    if (prop != null) {
      // set a order that gives priority to inherited properties
      // push Id/EmbeddedId up and CreatedTimestamp/UpdatedTimestamp down
      int sortOverride = prop.getSortOverride();
      prop.setSortOrder((10 * 10000 + 100 - index + sortOverride));

      DeployBeanProperty replaced = desc.addBeanProperty(prop);
      if (replaced != null && !replaced.isTransient()) {
        String msg = "Huh??? property " + prop.getFullBeanName() + " being defined twice";
        msg += " but replaced property was not transient? This is not expected?";
        CoreLog.log.log(WARNING, msg);
      }
    }
  }

  protected DeployBeanProperty createProp(DeployBeanDescriptor<?> desc, XField field, Class<?> beanType) {
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
      XGenericType genericType = (XGenericType) field.getAnnotation(GenericType.class);
      Type gType = genericType != null ? genericType.genericType() : field.getType();
      return new DeployBeanProperty(desc, propertyType, gType);
    }
    // check for Collection type (list, set or map)
    ManyType manyType = determineManyType.getManyType(propertyType);
    if (manyType != null) {
      // List, Set or Map based object
      Class<?> targetType = field.getTargetType();
      if (targetType == null) {
        if (field.has(Transient.class)) {
          // not supporting this field (generic type used)
          return null;
        }
        CoreLog.internal.log(WARNING, "Could not find parameter type (via reflection) on " + desc.getFullName() + " " + field.getName());
      }
      return createManyType(desc, targetType, manyType);
    }
    if (propertyType.isEnum() || propertyType.isPrimitive()) {
      return new DeployBeanProperty(desc, propertyType, null, null);
    }
    ScalarType<?> scalarType = typeManager.type(propertyType);
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
      CoreLog.log.log(ERROR, "Error with " + desc + " field:" + field.getName(), e);
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
      ScalarType<?> scalarType = typeManager.type(targetType);
      if (scalarType != null) {
        return new DeployBeanPropertySimpleCollection(desc, targetType, manyType);
      }
    } catch (NullPointerException e) {
      CoreLog.internal.log(DEBUG, "expected non-scalar type {}", e.getMessage());
    }
    return new DeployBeanPropertyAssocMany(desc, targetType, manyType);
  }

  public <T> DeployBeanInfo<T> createDeployBeanInfo(Class<?> beanClass, DeployBeanInfo info, XReadAnnotations readAnnotations) throws Exception {
    return createDeployBeanInfo(null, beanClass, info, readAnnotations);
  }

  public <T> DeployBeanInfo<T> createDeployBeanInfo(XEntity entity, Class<?> beanClass, DeployBeanInfo info, XReadAnnotations readAnnotations) throws Exception {
    if (info.getDescriptor().readCustomSlot().length == 0) {
      return info;
    }
    if (entity == null) {
      entity = entityProvider.getEntity(beanClass);
    }
    if (entity == null || entity.getFields().isEmpty()) {
      return info;
    }
    DeployBeanDescriptor desc = copyDescriptor(info.getDescriptor(), beanClass);
    createProperties(desc, entity, desc.getBeanType());
    setProperties(desc);
    info = new DeployBeanInfo<>(info.getUtil(), desc, entity);
    readAnnotations.readInitial(entity, info); //initial base scalar properties
    return info;
  }

  protected DeployBeanDescriptor<?> copyDescriptor(DeployBeanDescriptor descriptor, Class<?> beanClass) throws Exception {
    DeployBeanDescriptor<?> desc = new DeployBeanDescriptor<>(null, beanClass, null);
    Field[] fields = descriptor.getClass().getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);
      if (Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      if (field.getName().equals("propMap")) {
        field.set(desc, copyBeanProperty(desc, (LinkedHashMap<String, DeployBeanProperty>) field.get(descriptor)));
      } else if (field.getName().equals("properties")) {
        field.set(desc, null);
      } else if (field.getName().equals("beanType") || field.getName().equals("concurrencyMode")) {
        continue;
      } else {
        field.set(desc, field.get(descriptor));
      }
    }
    return desc;
  }

  protected LinkedHashMap<String, DeployBeanProperty> copyBeanProperty(DeployBeanDescriptor<?> desc, LinkedHashMap<String, DeployBeanProperty> propMap) {
    return new LinkedHashMap<>(propMap);
  }
}
