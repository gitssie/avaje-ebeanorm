package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import javax.persistence.Transient;
import java.lang.annotation.Annotation;

public class XTransient implements Transient {
  @Override
  public Class<? extends Annotation> annotationType() {
    return Transient.class;
  }
}
