package io.ebeaninternal.server.deploy.tenant;

import io.ebean.bean.XEntityProvider;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeaninternal.server.deploy.parse.tenant.XEntityFinder;
import io.ebeaninternal.server.deploy.parse.tenant.XField;
import io.ebeaninternal.server.deploy.parse.tenant.annotation.XTable;

public class XEntityProviderTest implements XEntityProvider {
  @Override
  public XEntityFinder create() {
    return new XEntityFinder() {

      @Override
      public XEntity getEntity(Object tenantId,Class<?> beanClass) {
        if (!beanClass.getName().endsWith("Customer")) {
          XEntity entity = new XEntity(beanClass);
          return entity;
        }
        XEntity entity = new XEntity(beanClass);
        entity.setName("Customer");
        entity.addAnnotation(new XTable("o_customer"));
        XField name = new XField("name__c", String.class);
        entity.addField(name);
        XField name2 = new XField("name2__c", String.class);
        entity.addField(name2);
        /**
        XField oneToOne = new XField("shippingAddress", Address.class);
//        oneToOne.addAnnotation(new XManyToOne(Address.class, CascadeType.ALL));
        entity.addField(oneToOne);

        XField oneToMany = new XField("contactsList", List.class);
        oneToMany.addAnnotation(new XManyToMany(Contact.class, null, FetchType.EAGER));
        entity.addField(oneToMany);
        **/
        return entity;
      }

      @Override
      public boolean isChanged(Object tenantId,Class<?> entityClass) {
        return false;
      }

      @Override
      public <S> S getServiceObject(Class<S> clazz) {
        return null;
      }
    };
  }
}
