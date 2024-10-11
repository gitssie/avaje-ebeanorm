package io.ebeaninternal.server.dto;

import io.ebean.core.type.DataReader;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DtoQueryPlanConMap extends DtoQueryPlanBase {
  private final DtoColumn[] dtoColumns;

  DtoQueryPlanConMap(DtoMappingRequest request, DtoColumn[] dtoColumns) {
    super(request);
    this.dtoColumns = dtoColumns;
  }

  @Override
  public Object readRow(DataReader dataReader) throws SQLException {
    Map<String, Object> bean = new HashMap<>();
    for (DtoColumn prop : dtoColumns) {
      bean.put(prop.label(), dataReader.getObject());
    }
    return bean;
  }

}
