package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import javax.persistence.Lob;
import java.lang.annotation.Annotation;

public class XLob implements Lob {
  @Override
  public Class<? extends Annotation> annotationType() {
    return Lob.class;
  }

  @Override
  public String toString(){
    return getClass().getSimpleName();
  }
}
