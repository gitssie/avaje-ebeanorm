package io.ebean.xtest.base;

import io.ebean.DB;
import io.ebean.DatabaseFactory;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.bean.EntityBean;
import io.ebean.test.LoggedSql;
import io.ebean.test.UserContext;
import io.ebean.xtest.BaseTestCase;
import io.ebeaninternal.server.TenantContainerFactory;
import io.ebeaninternal.server.core.DefaultServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import net.bytebuddy.ByteBuddy;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EbeanServer_eqlTest extends BaseTestCase {


  @Test
  public void testUpdate(){
    DefaultServer server = (DefaultServer) this.server();
    UserContext.setTenantId(1);
    int id = 1;
    Transaction trans = server.beginTransaction();
    Customer customer = new Customer();
    customer.setId(id++);
    customer.setName("客户A");

    Contact c1 = new Contact();
    c1.setId(id++);
    c1.setFirstName("联系人1");
    Contact c2 = new Contact();
    c2.setId(id++);
    c2.setFirstName("联系人2");

    List<Contact> cs = new ArrayList<>();
    cs.add(c1);
    cs.add(c2);

    customer.set("contactsList", cs);

    server.save(customer);
    System.out.println("-------save---------");
    c1 = new Contact();
    c1.setId(id++);
    c1.setFirstName("联系人1");

    cs.add(c1);
    server.update(customer);

    trans.commit();
  }


  @Test
  public void testOneToManySave(){
    DefaultServer server = (DefaultServer) this.server();
    UserContext.setTenantId(1);
    int id = 1;
    Transaction trans = server.beginTransaction();
    for(int j=0;j<10;j++) {
      Customer customer = new Customer();
      customer.setId(id++);
      customer.setName("客户A");

      Contact c1 = new Contact();
      c1.setId(id++);
      c1.setFirstName("联系人1");
      Contact c2 = new Contact();
      c2.setId(id++);
      c2.setFirstName("联系人2");

      List<Contact> cs = new ArrayList<>();
      cs.add(c1);
      cs.add(c2);

      customer.set("contactsList", cs);

      server.save(customer);
    }
    trans.commit();
    System.out.println("****************************************************");
    List<Customer> customer = server.createQuery(Customer.class).findList();
    BeanDescriptor beanDescriptor = server.getBeanDescriptorManager().descriptor(Customer.class);
    for (BeanPropertyAssocOne beanPropertyAssocOne : beanDescriptor.propertiesOne()) {
      System.out.println(beanPropertyAssocOne.getValue((EntityBean) customer.get(0)));
    }
    System.out.println(customer.size());
    System.out.println(beanDescriptor.propertiesOne());
  }

  @Test
  public void testOneToOneSave(){
    DefaultServer server = (DefaultServer) this.server();
    UserContext.setTenantId(1);
    Transaction trans = server.beginTransaction();
    Customer customer = new Customer();
    customer.setId(1);
    customer.setName("aaaa");

    Address address = new Address();
    address.setCity("深圳");
    address.setLine1("Line1");
    Customer c2 = new Customer();
    c2.setId(2);
    c2.set("name__c","客户关联客户-新数据");
    c2.setName("客户关联客户");

    customer.set("name__c","新数据");
    customer.set("shippingAddress",c2);

    server.save(customer);
    trans.commit();
    customer = server.find(Customer.class,1);
    c2 = (Customer) customer.get("shippingAddress");

    System.out.println(customer.getName());
    System.out.println(customer.get("name__c"));
    System.out.println(c2.getName());
    System.out.println(c2.get("name__c"));
  }

  @Test
  public void testORM(){
    DefaultServer server = (DefaultServer) this.server();
//    BeanDescriptorTenantManager manager = server.getBeanDescriptorManager();
//    manager.registerEntity(Customer.class);
    UserContext.setTenantId(1);
    Transaction trans = server.beginTransaction();
    Customer customer = new Customer();
    customer.setId(1);
    customer.setName("aaaa");
    customer.set("name__c","测试");
    Contact c = new Contact();
    c.setFirstName("Jack");
    c.setLastName("Mack");
    customer.addContact(c);
    server.save(customer);

    Address address = new Address();
    address.setCity("深圳");
    address.setLine1("Line1");

    customer = server.find(Customer.class,1);
    customer.set("name__c","新数据");
    customer.set("shippingAddress",address);

    server.update(customer);
    trans.commit();
    customer = server.find(Customer.class,1);
    System.out.println(customer.getName());
    System.out.println(customer.get("name__c"));
  }

  @Test
  public void basic() {

    ResetBasicData.reset();

    Query<Customer> query = server().createQuery(Customer.class, "order by id limit 10");
    query.setMaxRows(100);
    query.findList();

    if (isSqlServer()) {
      assertSql(query).startsWith("select top 100 ");
      assertSql(query).endsWith("order by t0.id");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains(" fetch next 100 rows only");
    } else {
      assertSql(query).endsWith("order by t0.id limit 100");
    }
  }

  @Test
  public void basic_via_Ebean_defaultServer() {

    ResetBasicData.reset();

    Query<Customer> query = DB.createQuery(Customer.class, "order by id limit 10");
    query.findList();

    if (isSqlServer()) {
      assertSql(query).startsWith("select top 10 ");
      assertSql(query).endsWith("order by t0.id");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains(" fetch next 10 rows only");
    } else {
      assertSql(query).endsWith("order by t0.id limit 10");
    }
  }

  @Test
  public void basic_limit_offset1() {

    ResetBasicData.reset();

    Query<Customer> query = DB.createQuery(Customer.class, "order by id limit 10 offset 3");
    query.findList();

    if (isSqlServer()) {
      assertSql(query).endsWith("order by t0.id offset 3 rows fetch next 10 rows only");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains("offset 3 rows fetch next 10 rows only");
    } else {
      assertSql(query).endsWith("order by t0.id limit 10 offset 3");
    }

  }

  @Test
  public void basic_limit_offset2() {

    ResetBasicData.reset();

    Query<Customer> query = DB.createQuery(Customer.class, "order by name");
    query.setMaxRows(10);
    query.setFirstRow(3);
    query.findList();

    if (isSqlServer()) {
      assertSql(query).endsWith("order by t0.name offset 3 rows fetch next 10 rows only");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains("offset 3 rows fetch next 10 rows only");
    } else {
      assertSql(query).endsWith("order by t0.name limit 10 offset 3");
    }

    // check also select count(*)
    LoggedSql.start();
    query.findCount();
    List<String> sql = LoggedSql.stop();
    assertThat(sql.get(0)).startsWith("select count(*) from o_customer t0;");
  }

  @Test
  public void basic_limit_offset2_with_id() {

    ResetBasicData.reset();

    Query<Customer> query = DB.createQuery(Customer.class, "order by name");
    query.setMaxRows(10);
    query.setFirstRow(3);
    query.orderById(true);
    query.findList();

    if (isSqlServer()) {
      assertSql(query).endsWith("order by t0.name, t0.id offset 3 rows fetch next 10 rows only");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains("offset 3 rows fetch next 10 rows only");
    } else {
      assertSql(query).endsWith("order by t0.name, t0.id limit 10 offset 3");
    }

    // check also select count(*)
    LoggedSql.start();
    query.findCount();
    List<String> sql = LoggedSql.stop();
    assertThat(sql.get(0)).startsWith("select count(*) from o_customer t0;");

  }

  @Test
  public void basic_limit_offset3() {

    ResetBasicData.reset();

    Query<Customer> query = DB.createQuery(Customer.class);
    query.setMaxRows(10);
    query.setFirstRow(3);
    if (isSqlServer()) {
      query.order("id");
    }
    query.findList();

    if (isSqlServer()) {
      assertSql(query).endsWith("from o_customer t0 order by t0.id offset 3 rows fetch next 10 rows only");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains("offset 3 rows fetch next 10 rows only");
    } else {
      assertSql(query).endsWith("from o_customer t0 limit 10 offset 3");
    }
  }

  @Test
  public void basic_limit_offset4() {

    ResetBasicData.reset();

    Query<Customer> query = DB.createQuery(Customer.class);
    query.setMaxRows(10);
    query.findList();

    if (isSqlServer()) {
      assertSql(query).startsWith("select top 10 ");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains("fetch next 10 rows only");
    } else {
      assertSql(query).endsWith("limit 10");
    }
  }

  @Test
  public void orderBy_override() {

    ResetBasicData.reset();

    Query<Customer> query = server().createQuery(Customer.class, "order by id");

    // use clear() and then effectively override the orderBy clause
    query.orderBy().clear().asc("name");
    query.findList();

    assertSql(query).contains("order by t0.name");
  }


  @Test
  public void namedParams() {

    ResetBasicData.reset();

    Query<Customer> query = server().createQuery(Customer.class, "where name startsWith :name order by name");
    query.setParameter("name", "Ro");
    query.findList();

    assertSql(query).contains("where t0.name like ");
  }

  @Test
  public void unboundNamedParams_expect_PersistenceException() {
    Query<Customer> query = server().createQuery(Customer.class, "where name = :name");
    assertThrows(PersistenceException.class, () ->query.findOne());
  }

  @Test
  public void namedQuery() {
    ResetBasicData.reset();

    Query<Customer> name = server().createNamedQuery(Customer.class, "name");
    name.findList();

    assertThat(sqlOf(name, 1)).contains("select t0.id, t0.name from o_customer t0 order by t0.name");
  }

  @Test
  public void namedQuery_withStatus() {

    ResetBasicData.reset();

    Query<Customer> name = server().createNamedQuery(Customer.class, "withStatus");
    name.order().clear().asc("status");
    name.findList();

    assertThat(sqlOf(name, 2)).contains("select t0.id, t0.name, t0.status from o_customer t0 order by t0.status");
  }

  @Test
  public void namedQuery_withContacts() {

    ResetBasicData.reset();

    Query<Customer> query = server()
      .createNamedQuery(Customer.class, "withContacts")
      .setParameter("id", 1);

    query.setUseCache(false);
    query.findOne();

    assertSql(query).contains("from o_customer t0 left join contact t1 on t1.customer_id = t0.id ");
  }

}
