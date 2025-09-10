package io.ebeaninternal.server.dto;

import io.ebean.SqlRow;
import java.util.*;

/**
 * Holds property and constructor meta-data for a given DTO bean type.
 * <p>
 * Uses this to map a mapping request (columns) to a 'query plan' (constructor and setters).
 */
final class DtoMeta {

  private final Class<?> dtoType;
  private final Map<String, DtoMetaProperty> propMap = new LinkedHashMap<>();
  private final Map<Integer, DtoMetaConstructor> constructorMap = new LinkedHashMap<>();
  private final DtoMetaConstructor defaultConstructor;
  private final DtoMetaConstructor maxArgConstructor;

  DtoMeta(Class<?> dtoType, Collection<DtoMetaConstructor> constructors, List<DtoMetaProperty> properties) {
    this.dtoType = dtoType;
    for (DtoMetaProperty property : properties) {
      propMap.put(property.name().toUpperCase(), property);
    }
    int maxArg = 0;
    DtoMetaConstructor defaultConstructor = null;
    DtoMetaConstructor maxArgConstructor = null;
    for (DtoMetaConstructor constructor : constructors) {
      int args = constructor.argCount();
      constructorMap.put(args, constructor);
      if (args == 0) {
        defaultConstructor = constructor;
      } else if (args > maxArg) {
        maxArgConstructor = constructor;
        maxArg = args;
      }
    }
    this.defaultConstructor = defaultConstructor;
    this.maxArgConstructor = maxArgConstructor;
  }

  public DtoQueryPlan match(DtoMappingRequest request) {
    DtoColumn[] cols = request.columnMeta();
    int colLen = cols.length;
    DtoMetaConstructor constructor = constructorMap.get(colLen);
    if (constructor != null) {
      return new DtoQueryPlanConstructor(request, constructor);
    }
    if (maxArgConstructor != null && colLen > maxArgConstructor.argCount()) {
      // maxArgConst + setters
      return matchMaxArgPlusSetters(request);
    }
    if (defaultConstructor != null && DtoQueryPlan.class.isAssignableFrom(dtoType)) {
      return (DtoQueryPlan) defaultConstructor.defaultConstructor();
    } else if (defaultConstructor != null) {
      return matchSetters(request);
    } else if (dtoType.equals(Map.class)) {
      return matchHashMap(request);
    } else if (dtoType.equals(SqlRow.class)) {
      return matchSqlRow(request);
    } else if(dtoType.isArray()){
      return matchArray(request);
    }
    String msg = "Unable to map the resultSet columns " + Arrays.toString(cols)
      + " to the bean type ["+dtoType+"] as the number of columns in the resultSet is less than the constructor"
      + " (and that there is no default constructor) ?";
    throw new IllegalStateException(msg);
  }

  private DtoQueryPlanConPlus matchMaxArgPlusSetters(DtoMappingRequest request) {
    DtoReadSet[] setterProps = request.mapArgPlusSetters(this, maxArgConstructor.argCount());
    return new DtoQueryPlanConPlus(request, maxArgConstructor, setterProps);
  }

  private DtoQueryPlan matchSetters(DtoMappingRequest request) {
    DtoReadSet[] setterProps = request.mapSetters(this);
    return new DtoQueryPlanConSetter(request, defaultConstructor, setterProps);
  }

  private DtoQueryPlan matchHashMap(DtoMappingRequest request) {
    DtoColumn[] dtoColumns = request.columnMeta();
    return new DtoQueryPlanConMap(request, dtoColumns);
  }

  private DtoQueryPlan matchSqlRow(DtoMappingRequest request) {
    DtoColumn[] dtoColumns = request.columnMeta();
    return new DtoQueryPlanConSqlRow(request, dtoColumns);
  }

  private DtoQueryPlan matchArray(DtoMappingRequest request) {
    DtoColumn[] dtoColumns = request.getColumnMeta();
    return new DtoQueryPlanConArray(request, dtoColumns);
  }

  DtoReadSet findProperty(String label) {
    String upperLabel = label.toUpperCase();
    DtoMetaProperty property = propMap.get(upperLabel);
    if (property == null && upperLabel.startsWith("IS_")) {
      property = propMap.get(upperLabel.substring(3));
    }
    if (property == null) {
      property = propMap.get(upperLabel.replace("_", ""));
    }
    return property;
  }

  Class<?> dtoType() {
    return dtoType;
  }
}
