package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarTypeBase;
import io.ebean.core.type.BasicTypeConverter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for BigDecimal.
 */
class ScalarTypeBigDecimal extends ScalarTypeBase<BigDecimal> {

  ScalarTypeBigDecimal() {
    super(BigDecimal.class, true, Types.DECIMAL);
  }

  @Override
  public void bind(DataBinder binder, BigDecimal value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.DECIMAL);
    } else {
      binder.setBigDecimal(value);
    }
  }

  @Override
  public BigDecimal read(DataReader reader) throws SQLException {
    return reader.getBigDecimal();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toBigDecimal(value);
  }

  @Override
  public BigDecimal toBeanType(Object value) {
    return BasicTypeConverter.toBigDecimal(value);
  }

  @Override
  public String formatValue(BigDecimal t) {
    return t.toPlainString();
  }

  @Override
  public BigDecimal parse(String value) {
    return new BigDecimal(value);
  }

  @Override
  public BigDecimal readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return BigDecimal.valueOf(dataInput.readDouble());
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, BigDecimal b) throws IOException {
    if (b == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeDouble(b.doubleValue());
    }
  }

  @Override
  public BigDecimal jsonRead(JsonParser parser) throws IOException {
    return parser.getDecimalValue();
  }

  @Override
  public void jsonWrite(JsonGenerator writer, BigDecimal value) throws IOException {
    writer.writeNumber(value);
  }

  @Override
  public DocPropertyType docType() {
    return DocPropertyType.DOUBLE;
  }

}
