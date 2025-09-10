package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.*;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeaninternal.server.deploy.parse.tenant.XField;
import io.ebeaninternal.server.deploy.parse.tenant.annotation.XTenantId;
import io.ebeaninternal.server.deploy.parse.tenant.generatedproperty.DefaultGeneratedProperty;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class XAnnotationFields extends AnnotationFields {
  private TenantDeployCreateProperties createProperties;
  private DefaultGeneratedProperty defaultValueGeneratedProperty = new DefaultGeneratedProperty();
  private transient XField field;

  XAnnotationFields(DeployBeanInfo<?> info, ReadAnnotationConfig readConfig, TenantDeployCreateProperties createProperties) {
    super(info, readConfig);
    this.createProperties = createProperties;
  }

  /**
   * Read the field level deployment annotations.
   */
  @Override
  public void parse() {
    XEntity entity = info.getEntity();
    if (entity == null) {
      return;
    }
    for (DeployBeanProperty prop : descriptor.propertiesAll()) {
      XField field = entity.getField(prop.getName());
      if (field == null) {
        continue;
      }
      parseField(field, prop);
    }
    addTenantProperty(entity);
  }

  private void parseField(XField field, DeployBeanProperty prop) {
    this.field = field;
    prop.setPropertyId(field.getId() == null ? 0 : field.getId());
    prop.initAnnotations(new HashSet<>(field.getAnnotations()));
    if (prop instanceof DeployBeanPropertyAssoc<?>) {
      readAssocOne((DeployBeanPropertyAssoc<?>) prop);
    } else {
      readField(prop);
    }
    this.field = null;
  }

  private void addTenantProperty(XEntity entity) {
    if (!entity.isTenant()) {
      return;
    }
    DeployBeanProperty prop = null;
    for (DeployBeanProperty property : descriptor.propertiesAll()) {
      if (property.isTenantId()) {
        return;
      } else if (property.getPropertyType() == Long.class && XTenantId.NAME.equals(property.getName())) {
        prop = property;
        break;
      }
    }
    XField field = new XField(XTenantId.NAME, Long.class);
    field.addAnnotation(new XTenantId());
    if (prop == null) {
      prop = createProperties.createProp(descriptor, field, descriptor.getBeanType());
    }
    parseField(field, prop);
    descriptor.addBeanProperty(prop);
  }

  @Override
  protected void initValidation(DeployBeanProperty prop) {
    prop.setNullable(field.isNullable());
    if (!prop.isLob()) {
      Integer maxSize = field.getMaxLength();
      if (maxSize != null && maxSize > 0) {
        prop.setDbLength(maxSize);
      }
      if (Number.class.isAssignableFrom(prop.getPropertyType())) {
        if (field.getIntegerLength() != null && field.getIntegerLength() > 0) {
          prop.setDbLength(field.getIntegerLength());
        }
        if (field.getDecimalLength() != null && field.getDecimalLength() > 0) {
          prop.setDbScale(field.getDecimalLength());
        }
      }
    }
  }

  @Override
  protected void initConvert(DeployBeanProperty prop) {
    super.initConvert(prop);
    if (has(prop, DbDefault.class)) {
      prop.setGeneratedProperty(defaultValueGeneratedProperty);
    }
  }

  @Override
  <T extends Annotation> T get(DeployBeanProperty prop, Class<T> annClass) {
    return field.getAnnotation(annClass);
  }

  @Override
  <T extends Annotation> boolean has(DeployBeanProperty prop, Class<T> annClass) {
    return field.has(annClass);
  }

  @Override
  Set<JoinColumn> annotationJoinColumns(DeployBeanProperty prop) {
    final JoinColumn col = field.getAnnotation(JoinColumn.class);
    if (col != null) {
      return Collections.singleton(col);
    }
    final JoinColumns cols = field.getAnnotation(JoinColumns.class);
    if (cols != null) {
      Set<JoinColumn> result = new LinkedHashSet<>();
      Collections.addAll(result, cols.value());
      return result;
    }
    return Collections.emptySet();
  }

  @Override
  Set<AttributeOverride> annotationAttributeOverrides(DeployBeanProperty prop) {
    final AttributeOverride ann = field.getAnnotation(AttributeOverride.class);
    if (ann != null) {
      return Collections.singleton(ann);
    }
    final AttributeOverrides collection = field.getAnnotation(AttributeOverrides.class);
    if (collection != null) {
      Set<AttributeOverride> result = new LinkedHashSet<>();
      Collections.addAll(result, collection.value());
      return result;
    }
    return Collections.emptySet();
  }

  @Override
  Set<Index> annotationIndexes(DeployBeanProperty prop) {
    final Index ann = field.getAnnotation(Index.class);
    if (ann != null) {
      return Collections.singleton(ann);
    }
    final Indices collection = field.getAnnotation(Indices.class);
    if (collection != null) {
      Set<Index> result = new LinkedHashSet<>();
      Collections.addAll(result, collection.value());
      return result;
    }
    return Collections.emptySet();
  }

  @Override
  Set<DbMigration> annotationDbMigrations(DeployBeanProperty prop) {
    final DbMigration ann = field.getAnnotation(DbMigration.class);
    if (ann != null) {
      return Collections.singleton(ann);
    }
    final DbMigration.List collection = field.getAnnotation(DbMigration.List.class);
    if (collection != null) {
      Set<DbMigration> result = new LinkedHashSet<>();
      Collections.addAll(result, collection.value());
      return result;
    }
    return Collections.emptySet();
  }
}
