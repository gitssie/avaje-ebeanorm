package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.DbMigration;
import io.ebean.annotation.Index;
import io.ebean.annotation.Indices;
import io.ebeaninternal.server.deploy.BeanDescriptorMap;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeaninternal.server.deploy.parse.tenant.XField;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class XAnnotationAssocOnes extends AnnotationAssocOnes {
  private transient XField field;

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
    XEntity entity = info.getEntity();
    if (entity == null) {
      return;
    }
    for (DeployBeanProperty prop : descriptor.propertiesAll()) {
      if (prop instanceof DeployBeanPropertyAssocOne<?>) {
        if (prop.getField() == null) {
          XField field = entity.getField(prop.getName());
          parseField(field, (DeployBeanPropertyAssocOne<?>) prop);
        }
      }
    }
  }

  private void parseField(XField field, DeployBeanPropertyAssocOne<?> prop) {
    this.field = field;
    readAssocOne(prop);
    this.field = null;
  }

  @Override
  protected void initValidation(DeployBeanPropertyAssocOne<?> prop) {
    prop.setNullable(field.isNullable());
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
