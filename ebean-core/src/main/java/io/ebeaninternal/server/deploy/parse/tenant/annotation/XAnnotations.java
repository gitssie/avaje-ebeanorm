package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import java.util.HashMap;
import java.util.Map;

public class XAnnotations {
  private Map<String, Class<?>> annotationClass = new HashMap<>();
  private Map<String, Class<?>> annotationEntityClass = new HashMap<>();

  public XAnnotations() {
    this.initClassMap();
  }

  private void initClassMap() {
    annotationClass.put("XColumn", XColumn.class);
    annotationClass.put("DbDefault", XDbDefault.class);
    annotationClass.put("DBJson", XDbJson.class);
    annotationClass.put("DBMap", XDbMap.class);
    annotationClass.put("GeneratedValue", XGeneratedValue.class);
    annotationClass.put("Index", XIndex.class);
    annotationClass.put("JoinColumn", XJoinColumn.class);
    annotationClass.put("Lob", XLob.class);
    annotationClass.put("TenantId", XTenantId.class);
    annotationClass.put("Transient", XTransient.class);
    annotationClass.put("Version", XVersion.class);

    annotationClass.put("ManyToMany", XManyToMany.class);
    annotationClass.put("ManyToOne", XManyToOne.class);
    annotationClass.put("OneToMany", XOneToMany.class);
    annotationClass.put("OneToOne", XOneToOne.class);

    annotationClass.put("ChangeLog", XChangeLog.class);

  }

  public Class<?> getClass(String name) {
    return annotationClass.get(name);
  }

  public Class<?> getEntityAnnotationClass(String name) {
    return annotationEntityClass.get(name);
  }
}
