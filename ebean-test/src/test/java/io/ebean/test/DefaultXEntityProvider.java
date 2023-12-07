package io.ebean.test;

import io.ebean.bean.XEntityProvider;
import io.ebean.config.AutoConfigure;
import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeaninternal.server.deploy.parse.tenant.XEntityFinder;
import io.ebeaninternal.server.deploy.parse.tenant.XField;
import io.ebeaninternal.server.deploy.parse.tenant.annotation.*;
import net.bytebuddy.ByteBuddy;
import org.tests.model.basic.Address;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.util.List;

public class DefaultXEntityProvider implements XEntityProvider, AutoConfigure {
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
        entity.addAnnotation(new XTable("o_customer"));
        XField name = new XField("name__c", String.class);
        entity.addField(name);
        XField name2 = new XField("name2__c", String.class);
        entity.addField(name2);
        XField oneToOne = new XField("shippingAddress", Address.class);
//        oneToOne.addAnnotation(new XManyToOne(Address.class, CascadeType.ALL));
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

  @Override
  public void preConfigure(DatabaseConfig config) {
    config.putServiceObject(XEntityProvider.class.getName(),this);
  }

  @Override
  public void postConfigure(DatabaseConfig config) {

  }
}
