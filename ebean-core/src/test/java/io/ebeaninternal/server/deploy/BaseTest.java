package io.ebeaninternal.server.deploy;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.annotation.Platform;
import io.ebean.bean.XEntityProvider;
import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeaninternal.server.deploy.parse.tenant.XEntityFinder;
import io.ebeaninternal.server.deploy.parse.tenant.XField;
import io.ebeaninternal.server.deploy.parse.tenant.annotation.XManyToMany;
import io.ebeaninternal.server.deploy.parse.tenant.annotation.XTable;
import io.ebeaninternal.server.deploy.tenant.CurrentTenantProviderTest;
import io.ebeaninternal.server.deploy.tenant.XEntityProviderTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.tests.model.basic.Address;
import org.tests.model.basic.Animal;
import org.tests.model.basic.Contact;

import javax.persistence.FetchType;
import javax.persistence.PersistenceException;
import java.util.List;

public class BaseTest {

  protected SpiEbeanServer db;

  public BaseTest() {
    DatabaseConfig config = new DatabaseConfig();
    config.setName("h2");
    config.loadFromProperties();
    config.setCurrentTenantProvider(new CurrentTenantProviderTest());
    config.putServiceObject(XEntityProvider.class.getName(), new XEntityProviderTest());
    this.db = (SpiEbeanServer) DatabaseFactory.create(config);
  }

  protected <T> BeanDescriptor<T> getBeanDescriptor(Class<T> cls) {
    return db.descriptor(cls);
  }

  protected SpiEbeanServer spiEbeanServer() {
    return db;
  }

  protected Database server() {
    return db;
  }

  protected void initTables() {
    try {
      db.find(Animal.class).findCount();
    } catch (PersistenceException e) {
      db.script().run("/h2-init.sql");
    }
  }

  protected boolean isH2() {
    return isPlatform(Platform.H2);
  }

  protected boolean isPostgres() {
    return isPlatform(Platform.POSTGRES);
  }

  protected boolean isSqlServer() {
    return isPlatform(Platform.SQLSERVER);
  }

  protected boolean isMySql() {
    return isPlatform(Platform.MYSQL);
  }

  protected boolean isPlatform(Platform platform) {
    return db.platform().base().equals(platform);
  }
}
