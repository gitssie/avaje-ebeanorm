package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import com.avaje.ebean.config.dbplatform.PostgresPlatform;
import com.avaje.ebean.text.json.EJson;
import com.avaje.ebean.text.TextException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Postgres Hstore type which maps Map<String,String> to a single 'HStore column' in the DB.
 */
@SuppressWarnings("rawtypes")
public class ScalarTypePostgresHstore extends ScalarTypeBase<Map> {

  public static final String KEY = "hstore";
  
  public static final int HSTORE_TYPE = PostgresPlatform.TYPE_HSTORE;
  
  public ScalarTypePostgresHstore() {
    super(Map.class, false, HSTORE_TYPE);
  }
  
  @Override
  public boolean isMutable() {
    return true;
  }
  
  @Override
  public boolean isDirty(Object value) {
    if (value instanceof ModifyAwareOwner) {
      return ((ModifyAwareOwner)value).isMarkedDirty();
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map read(DataReader dataReader) throws SQLException {
    
    Object value = dataReader.getObject();
    if (value == null) {
      return null;
    }
    if (value instanceof Map == false) {
      throw new RuntimeException("Expecting Hstore to return as Map but got type "+value.getClass());
    }
    return new ModifyAwareMap((Map)value);
  }

  @Override
  public void bind(DataBind b, Map value) throws SQLException {
    b.setObject(value);
  }

  @Override
  public Object toJdbcType(Object value) {
    return value;
  }

  @Override
  public Map toBeanType(Object value) {
    return (Map)value;
  }

  @Override
  public String formatValue(Map v) {
    try {
      return EJson.write(v);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  @Override
  public Map parse(String value) {
    try {
      return EJson.parseObject(value);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  @Override
  public Map parseDateTime(long dateTime) {
    throw new RuntimeException("Should never be called");
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public Object readData(DataInput dataInput) throws IOException {
    String json = dataInput.readUTF();
    return parse(json);
  }

  @Override
  public void writeData(DataOutput dataOutput, Object v) throws IOException {
    String json = format(v);
    dataOutput.writeUTF(json);
  }

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, Object value) throws IOException {
    EJson.write(value, ctx);
  }

  @Override
  public Object jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    return EJson.parseObject(ctx);
  }
  
}
