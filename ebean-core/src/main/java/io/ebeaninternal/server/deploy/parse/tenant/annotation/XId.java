package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import javax.persistence.Id;
import java.lang.annotation.Annotation;

public class XId implements Id {

  @Override
  public Class<? extends Annotation> annotationType() {
    return Id.class;
  }
}
