package org.tests.persistencecontext;

import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPersistenceContextOnUpdateDuringTxn extends BaseTestCase {

  @Test
  public void test() {
    Database server = DB.getDefault();

    try (Transaction txn = server.beginTransaction()) {
      EBasic bean1 = new EBasic();
      bean1.setName("hello");

      server.save(bean1);

      EBasic updatedEntity = new EBasic();
      updatedEntity.setId(bean1.getId());
      updatedEntity.setName("hello-changed");

      server.update(updatedEntity);

      // actually the bean is not in the persistence context so ...  the assert is fine
      EBasic loadedEntity = server.find(EBasic.class, bean1.getId());

      assertThat(loadedEntity.getName()).isEqualTo("hello-changed");
    }
  }

}
