package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import io.ebean.bean.ToStringBuilder;

import jakarta.persistence.Transient;
import java.lang.annotation.Annotation;

public class XTransient implements Transient {
  @Override
  public Class<? extends Annotation> annotationType() {
    return Transient.class;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
