package io.ebeaninternal.server.type;

import io.ebean.core.type.ScalarTypeUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ScalarTypeDurationWithNanosTest {

  ScalarTypeDurationWithNanos type = new ScalarTypeDurationWithNanos();

  @Test
  void testReadData() throws Exception {
    Duration duration = Duration.ofSeconds(323, 1500000);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(os);

    type.writeData(out, duration);
    type.writeData(out, null);
    out.flush();
    out.close();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    ObjectInputStream in = new ObjectInputStream(is);

    Duration val1 = type.readData(in);
    Duration val2 = type.readData(in);

    assertEquals(duration, val1);
    assertNull(val2);
  }

  @Test
  void testToJdbcType() throws Exception {
    Duration duration = Duration.ofSeconds(323, 1500000);
    BigDecimal bigDecimal = ScalarTypeUtils.toDecimal(duration);

    Object val1 = type.toJdbcType(duration);
    Object val2 = type.toJdbcType(bigDecimal);

    assertEquals(bigDecimal, val1);
    assertEquals(bigDecimal, val2);
  }

  @Test
  void testToBeanType() throws Exception {
    Duration duration = Duration.ofSeconds(323, 1500000);
    BigDecimal bigDecimal = ScalarTypeUtils.toDecimal(duration);

    Duration val1 = type.toBeanType(duration);
    Duration val2 = type.toBeanType(bigDecimal);

    assertEquals(duration, val1);
    assertEquals(duration, val2);
  }

  @Test
  void testFormatValue() {
    Duration duration = Duration.ofSeconds(323, 1500000);
    String formatValue = type.formatValue(duration);
    assertEquals("PT5M23.0015S", formatValue);
  }

  @Test
  void testParse() {
    Duration duration = Duration.ofSeconds(323, 1500000);
    Duration val1 = type.parse("PT5M23.0015S");
    assertEquals(duration, val1);
  }

  @Test
  void testJsonRead() throws Exception {
    Duration duration = Duration.ofSeconds(323, 1500000);

    JsonTester<Duration> jsonTester = new JsonTester<>(type);
    jsonTester.test(duration);
  }

}
