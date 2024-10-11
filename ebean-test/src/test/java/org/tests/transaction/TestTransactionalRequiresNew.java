package org.tests.transaction;

import io.ebean.*;
import io.ebean.annotation.Transactional;
import io.ebean.annotation.TxType;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestTransactionalRequiresNew extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestTransactionalRequiresNew.class);

  private Connection outerConn;
  private Transaction outerTxn;

  @Test
  public void basic() {

    outerTxn = null;
    assertNull(DB.currentTransaction());

    new OuterTransactionalWithRequired().doOuter();

    assertNull(DB.currentTransaction());
    assertNotNull(outerTxn);
  }


  class OuterTransactionalWithRequired {

    @Transactional
    void doOuter() {
      outerTxn = DB.currentTransaction();
      log.info("outer before ...{}", outerTxn);
      outerConn = outerTxn.connection();

      new InTransactionalWithRequiresNew().doInner();

      // restore the outerTxn
      Transaction current = DB.currentTransaction();
      log.info("outer after ...{}", current);
      assertSame(outerConn, current.connection());
    }

  }

  class InTransactionalWithRequiresNew {

    @Transactional(type = TxType.REQUIRES_NEW)
    void doInner() {
      Transaction innerTxn = DB.currentTransaction();
      log.info("inner ...{} {}", innerTxn);

      Connection connection = innerTxn.connection();
      assertNotSame(connection, outerConn);
    }
  }

  @Test
  public void testDifferentCreation1() {
    Database server = server();
    try(Transaction txn = server.beginTransaction()) {
      String txnName = txn.toString();
      try (Transaction txn2 = DB.beginTransaction(TxScope.requiresNew())) {
        txn2.commit();
      }
      assertThat(txn.toString()).isEqualTo(txnName);
    }
  }

  @Test
  public void testDifferentCreation2() {
    Database server = server();
    try (Transaction txn = server.beginTransaction()) {
      String txnName = txn.toString();
      try (Transaction txn2 = server.beginTransaction(TxScope.requiresNew())) {
        txn2.commit();
      }
      assertThat(txn.toString()).isEqualTo(txnName);
    }
  }

  @Test
  public void testDifferentCreation3() {
    Database server = server();
    try (Transaction txn = server.beginTransaction()) {
      String txnName = txn.toString();
      try (Transaction txn2 = DB.beginTransaction(TxScope.requiresNew())) {
        txn2.commit();
      }
      assertThat(txn.toString()).isEqualTo(txnName);
    }
  }

  @Test
  public void testDifferentCreation4() {
    Database server = server();
    try (Transaction txn = server.beginTransaction()) {
      String txnName = txn.toString();
      server.beginTransaction(TxScope.requiresNew());
      try {
        Transaction.current().commitAndContinue();
      } finally {
        Transaction.current().end();
      }
      assertThat(txn.toString()).isEqualTo(txnName);
    }
  }

}
