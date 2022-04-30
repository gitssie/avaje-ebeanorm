package io.ebeaninternal.server.deploy.parse.tenant;

import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.parse.tenant.annotation.XId;

public class XEntityProvider {

  public XEntity getEntity(DeployBeanDescriptor<?> desc) {
    XEntity entity = new XEntity(desc.getBeanType());
    XField id = new XField("id",Integer.class);
    id.addAnnotation(new XId());
    XField name = new XField("name",String.class);
    entity.addField(id);
    entity.addField(name);
    entity.addField(new XField("name__c",String.class));
    return entity;
  }
}
