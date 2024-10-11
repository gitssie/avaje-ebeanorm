package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.Platform;
import io.ebean.annotation.Where;
import io.ebean.config.BeanNotRegisteredException;
import io.ebeaninternal.server.deploy.BeanDescriptorManager;
import io.ebeaninternal.server.deploy.BeanDescriptorMap;
import io.ebeaninternal.server.deploy.BeanTable;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import io.ebeaninternal.server.deploy.parse.tenant.XField;

import java.lang.annotation.Annotation;

abstract class XAnnotationAssoc extends AnnotationParser {

  final BeanDescriptorMap factory;

  XAnnotationAssoc(DeployBeanInfo<?> info, ReadAnnotationConfig readConfig, BeanDescriptorMap factory) {
    super(info, readConfig);
    this.factory = factory;
  }

  void setTargetType(Class<?> targetType, DeployBeanPropertyAssoc<?> prop) {
    if (!targetType.equals(void.class)) {
      prop.setTargetType(targetType);
    }
  }

  void setBeanTable(DeployBeanPropertyAssoc<?> prop) {
    BeanTable assoc = getBeanTable(prop);
    if (assoc == null) {
      throw new BeanNotRegisteredException(errorMsgMissingBeanTable(prop.getTargetType(), prop.toString()));
    }
    prop.setBeanTable(assoc);
  }

  BeanTable getBeanTable(DeployBeanPropertyAssoc<?> prop) {
    return factory.beanTable(prop.getTargetType());
  }

  private String errorMsgMissingBeanTable(Class<?> type, String from) {
    return "Error with association to [" + type + "] from [" + from + "]. Is " + type + " registered? See https://ebean.io/docs/trouble-shooting#not-registered";
  }

  public Where getMetaAnnotationWhere(XField field, Platform platform) {
    Where fallback = null;
    for (Annotation ann : field.getAnnotations()) {
      if (ann.annotationType() == Where.class) {
        Where where = (Where) ann;
        final Platform[] platforms = where.platforms();
        if (platforms.length == 0) {
          fallback = where;
        } else if (matchPlatform(where.platforms(), platform)) {
          return where;
        }

      } else if (ann.annotationType() == Where.List.class) {
        Where.List whereList = (Where.List) ann;
        for (Where where : whereList.value()) {
          final Platform[] platforms = where.platforms();
          if (platforms.length == 0) {
            fallback = where;
          } else if (matchPlatform(where.platforms(), platform)) {
            return where;
          }
        }
      }
    }
    return fallback;
  }

  private boolean matchPlatform(Platform[] platforms, Platform match) {
    for (Platform platform : platforms) {
      if (platform == match) {
        return true;
      }
    }
    return false;
  }
}
