package io.ebean.xtest.base;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.*;
import io.ebean.bean.EntityBean;
import io.ebean.test.LoggedSql;
import io.ebean.test.UserContext;
import io.ebean.xtest.BaseTestCase;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.TenantContainerFactory;
import io.ebeaninternal.server.core.DefaultServer;
import io.ebeaninternal.server.core.SpiOrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanDescriptorManagerProvider;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import javax.persistence.PersistenceException;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EbeanServer_eqlTest extends BaseTestCase {
  protected Class<?> generateCustomEntityClass(ClassLoader parentClassLoader, Class<?> parent, String className) throws NoSuchMethodException {
    System.out.println(Modifier.isAbstract(parent.getMethod("_ebean_newInstance").getModifiers()));
    return new ByteBuddy()
      .subclass(parent)
      .name(parent.getPackageName() + "." + className)
      .make()
      .load(parentClassLoader)
      .getLoaded();
  }

  @Test
  public void testCustomUpdate() throws Exception {
    DefaultServer server = (DefaultServer) this.server();
    BeanDescriptorManagerProvider provider = server.config().getServiceObject(BeanDescriptorManagerProvider.class);
    UserContext.setTenantId(1);
    int id = 1;
    Class<?> customClass = generateCustomEntityClass(getClass().getClassLoader(), Customer.class, "QCustomer");
    Customer customer = (Customer) customClass.getDeclaredConstructor().newInstance();

    customer.setId(id++);
    customer.setName("客户A");

    server.save(customer);

    customer = (Customer) server.find(customClass, 1);
    customer.setName("客户B");
    server.update(customer);
    server.delete(customer);
    System.out.println(customer.getClass());
    System.out.println(customer);

    provider.redeploy(1, customClass, new XEntity());
  }


  @Test
  public void testUpdate() {
    DefaultServer server = (DefaultServer) this.server();
    UserContext.setTenantId(1);
    int id = 1;
    Transaction trans = server.beginTransaction();
    Customer customer = new Customer();
    customer.setId(id++);
    customer.setName("客户A");


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

  //@Test
  public void basicSQL() {

    ResetBasicData.reset();

    String sql = "select name where billingAddress.line1='JACK' order by id limit 100";
    sql = "where billingAddress.line1='JACK' order by id limit 100";

    Query<Customer> query = server().createQuery(Customer.class, sql);
    query.setMaxRows(0);

    ExpressionList where = query.where();
    query.findList();

    //SpiOrmQueryRequest<?> request = buildQueryRequest(SpiQuery.Type.LIST, query, transaction);

    assertSql(query).endsWith("order by t0.id limit 100");
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
      query.orderBy("id");
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
    name.orderBy().clear().asc("status");
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
