package io.ebeaninternal.server.dto;

import io.ebean.core.type.DataReader;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DtoQueryPlanConSqlRow extends DtoQueryPlanBase {
  private final String[] dtoColumns;

  DtoQueryPlanConSqlRow(DtoMappingRequest request, DtoColumn[] dtoColumns) {
    super(request);
    this.dtoColumns = new String[dtoColumns.length];
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < dtoColumns.length; i++) {
      this.dtoColumns[i] = toCamelCase(buf, dtoColumns[i].getLabel());
    }
  }

  @Override
  public Object readRow(DataReader dataReader) throws SQLException {
    Map<String, Object> bean = new HashMap<>();
    for (String prop : dtoColumns) {
      bean.put(prop, dataReader.getObject());
    }
    return bean;
  }

  private String toCamelCase(StringBuilder buf, String source) {
    return toCamelCase(buf, source, '_');
  }

  private String toCamelCase(StringBuilder buf, String source, char delimiter) {
    buf.setLength(0);
    char ch, ch2;
    for (int i = 0; i < source.length(); i++) {
      if (i == 0) {
        buf.append(Character.toLowerCase(source.charAt(i)));
        continue;
      }
      ch = source.charAt(i);
      if (ch == delimiter) {
        if (i + 1 < source.length()) {
          ch2 = source.charAt(++i);
          if (ch2 == delimiter) {
            buf.append(ch);
            buf.append(ch2);
          } else if (Character.isLowerCase(ch2)) {
            buf.append(Character.toUpperCase(ch2));
          } else {
            buf.append(ch2);
          }
        } else {
          buf.append(ch);
        }
      } else {
        buf.append(ch);
      }
    }
    return buf.toString();
  }
}
