package io.ebeaninternal.server.dto;

import io.ebean.core.type.DataReader;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DtoQueryPlanConArray extends DtoQueryPlanBase {
  private final DtoColumn[] dtoColumns;

  DtoQueryPlanConArray(DtoMappingRequest request, DtoColumn[] dtoColumns) {
    super(request);
    this.dtoColumns = dtoColumns;
  }

  @Override
  public Object readRow(DataReader dataReader) throws SQLException {
    Object[] row = new Object[dtoColumns.length];
    for (int i = 0; i < dtoColumns.length; i++) {
      row[i] = dataReader.getObject();
    }
    return row;
  }
}
