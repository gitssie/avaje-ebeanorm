package io.ebeaninternal.server.deploy.parse.tenant;

import java.util.*;

public class XEntity {
  private Class<?> beanType;
  private Map<String,XField> fields;

  public XEntity(Class<?> beanType) {
    this.beanType = beanType;
    this.fields = new HashMap<>();
  }

  public Class<?> getBeanType() {
    return beanType;
  }

  public Collection<XField> getFields() {
    return fields.values();
  }

  public void addField(XField field) {
    fields.put(field.getName(),field);
  }

  public XField getField(String name) {
    return fields.get(name);
  }
}
