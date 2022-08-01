package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import javax.persistence.Version;
import java.lang.annotation.Annotation;

public class XVersion implements Version {
  @Override
  public Class<? extends Annotation> annotationType() {
    return Version.class;
  }
}
