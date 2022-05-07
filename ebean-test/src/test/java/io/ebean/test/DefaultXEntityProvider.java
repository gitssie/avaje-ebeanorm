package io.ebean.test;

import io.ebean.bean.XEntityProvider;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeaninternal.server.deploy.parse.tenant.XEntityFinder;
import io.ebeaninternal.server.deploy.parse.tenant.XField;
import io.ebeaninternal.server.deploy.parse.tenant.annotation.*;
import org.tests.model.basic.Address;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.util.List;

public class DefaultXEntityProvider implements XEntityProvider {
  @Override
  public XEntityFinder create() {
    return new XEntityFinder() {

      @Override
      public XEntity getEntity(Class<?> beanClass) {
        if (!beanClass.getName().endsWith("Customer")) {
          XEntity entity = new XEntity(beanClass);
          return entity;
        }

        XEntity entity = new XEntity(beanClass);
        XField name = new XField("name__c", String.class);
        entity.addField(name);
        XField oneToOne = new XField("shippingAddress", Customer.class);
        oneToOne.addAnnotation(new XManyToOne(Customer.class, CascadeType.ALL));
        entity.addField(oneToOne);

        XField oneToMany = new XField("contactsList", List.class);
        oneToMany.addAnnotation(new XManyToMany(Contact.class,null, FetchType.EAGER));
        entity.addField(oneToMany);

        return entity;
      }

      @Override
      public boolean isChanged(Class<?> entityClass) {
        return false;
      }
    };
  }
}
