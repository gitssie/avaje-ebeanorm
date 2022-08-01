package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class XGenericType implements GenericType {
  private Type type;

  public XGenericType() {
  }

  public XGenericType(Type type) {
    this.type = type;
  }

  //@Override
  public Type genericType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return GenericType.class;
  }
}
