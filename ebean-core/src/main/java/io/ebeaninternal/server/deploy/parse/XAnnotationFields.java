package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.Index;
import io.ebean.annotation.*;
import io.ebean.config.EncryptDeploy;
import io.ebean.config.EncryptDeploy.Mode;
import io.ebean.config.dbplatform.DbEncrypt;
import io.ebean.config.dbplatform.DbEncryptFunction;
import io.ebean.config.dbplatform.IdType;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.deploy.DbMigrationInfo;
import io.ebeaninternal.server.deploy.IndexDefinition;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedPropertyFactory;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssoc;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.parse.tenant.XEntity;
import io.ebeaninternal.server.deploy.parse.tenant.XField;
import io.ebeaninternal.server.deploy.parse.tenant.annotation.XGeneratedValue;
import io.ebeaninternal.server.deploy.parse.tenant.annotation.XTenantId;
import io.ebeaninternal.server.deploy.parse.tenant.generatedproperty.DefaultGeneratedProperty;
import io.ebeaninternal.server.type.DataEncryptSupport;
import io.ebeaninternal.server.type.ScalarTypeBytesBase;
import io.ebeaninternal.server.type.ScalarTypeBytesEncrypted;
import io.ebeaninternal.server.type.ScalarTypeEncryptedWrapper;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Types;
import java.util.*;

import static io.ebean.util.AnnotationUtil.get;

/**
 * Read the field level deployment annotations.
 */
final class XAnnotationFields extends AnnotationParser {
  private final XEntity entity; //transient
  private final TenantDeployCreateProperties createProperties;
  /**
   * If present read Jackson JsonIgnore.
   */
  private final boolean jacksonAnnotationsPresent;
  private final GeneratedPropertyFactory generatedPropFactory;
  /**
   * By default we lazy load Lob properties.
   */
  private FetchType defaultLobFetchType = FetchType.LAZY;

  XAnnotationFields(XEntity entity, DeployBeanInfo<?> info, ReadAnnotationConfig readConfig, TenantDeployCreateProperties createProperties) {
    super(info, readConfig);
    this.entity = entity;
    this.createProperties = createProperties;
    this.jacksonAnnotationsPresent = readConfig.isJacksonAnnotations();
    this.generatedPropFactory = readConfig.getGeneratedPropFactory();
    if (readConfig.isEagerFetchLobs()) {
      defaultLobFetchType = FetchType.EAGER;
    }
  }

  /**
   * Read the field level deployment annotations.
   */
  @Override
  public void parse() {
    for (DeployBeanProperty prop : descriptor.propertiesAll()) {
      XField field = entity.getField(prop.getName());
      if (field == null) {
        continue;
      }
      prop.initAnnotations(new HashSet<>(field.getAnnotations()));
      if (prop instanceof DeployBeanPropertyAssoc<?>) {
        readAssocOne(field, (DeployBeanPropertyAssoc<?>) prop);
      } else {
        readField(field, prop);
      }
    }
    addTenantProperty();
  }

  private void addTenantProperty() {
    if (!entity.isTenant()) {
      return;
    }
    DeployBeanProperty prop = null;
    for (DeployBeanProperty property : descriptor.propertiesAll()) {
      if (property.isTenantId()) {
        return;
      } else if (property.getPropertyType() == Long.class && XTenantId.NAME.equals(property.getName())) {
        prop = property;
        break;
      }
    }
    XField field = new XField(XTenantId.NAME, Long.class);
    field.addAnnotation(new XTenantId());
    if(prop == null) {
      prop = createProperties.createProp(descriptor, field, descriptor.getBeanType());
    }
    //prop.setNullable(true);
    prop.initAnnotations(new HashSet<>(field.getAnnotations()));
    readField(field, prop);
    descriptor.addBeanProperty(prop);
  }

  private void readPropGenValue(XField field, DeployBeanProperty prop) {
    if (prop.isId()) {
      return;
    }
    GeneratedValue gen = field.getAnnotation(GeneratedValue.class);
    if (gen != null && gen instanceof XGeneratedValue) {
      GeneratedProperty genProp = ((XGeneratedValue) gen).getGeneratedProperty();
      if (genProp != null) {
        prop.setGeneratedProperty(genProp);
      }
    }
  }

  /**
   * Read the Id marker annotations on EmbeddedId properties.
   */
  private void readAssocOne(XField field, DeployBeanPropertyAssoc<?> prop) {
    readJsonAnnotations(field, prop);
    if (field.has(Id.class)) {
      readIdAssocOne(prop);
    }
    if (field.has(EmbeddedId.class)) {
      prop.setId();
      prop.setNullable(false);
      prop.setEmbedded();
      info.setEmbeddedId(prop);
    }
    DocEmbedded docEmbedded = field.getAnnotation(DocEmbedded.class);
    if (docEmbedded != null) {
      prop.setDocStoreEmbedded(docEmbedded.doc());
      if (descriptor.isDocStoreOnly()) {
        if (field.has(ManyToOne.class)) {
          prop.setEmbedded();
          prop.setDbInsertable(true);
          prop.setDbUpdateable(true);
        }
      }
    }
    if (prop instanceof DeployBeanPropertyAssocOne<?>) {
      if (prop.isId() && !prop.isEmbedded()) {
        prop.setEmbedded();
      }
//      readEmbeddedAttributeOverrides((DeployBeanPropertyAssocOne<?>) prop);
    }
    Formula formula = getMetaAnnotationFormula(field, platform);
    if (formula != null) {
      prop.setSqlFormula(processFormula(formula.select()), processFormula(formula.join()));
    }
    initWhoProperties(field, prop);
    initDbMigration(field, prop);
  }

  private void initWhoProperties(XField field, DeployBeanProperty prop) {
    if (field.has(WhoModified.class)) {
      generatedPropFactory.setWhoModified(prop);
    }
    if (field.has(WhoCreated.class)) {
      generatedPropFactory.setWhoCreated(prop);
    }
    if (field.has(TenantId.class)) {
      generatedPropFactory.setWhoTenant(prop);
    }
  }

  private void readField(XField field, DeployBeanProperty prop) {
    // all Enums will have a ScalarType assigned...
    boolean isEnum = prop.getPropertyType().isEnum();
    Enumerated enumerated = field.getAnnotation(Enumerated.class);
    if (isEnum || enumerated != null) {
      util.setEnumScalarType(enumerated, prop);
    }
    // its persistent and assumed to be on the base table
    // rather than on a secondary table
    prop.setDbRead(true);
    prop.setDbInsertable(true);
    prop.setDbUpdateable(true);
    Column column = field.getAnnotation(Column.class);
    if (column != null) {
      readColumn(column, prop);
    }
    readJsonAnnotations(field, prop);
    if (prop.getDbColumn() == null) {
      // No @Column or @Column.name() so use NamingConvention
      prop.setDbColumn(namingConvention.getColumnFromProperty(beanType, prop.getName()));
    }
    initIdentity(field, prop);
    initTenantId(field, prop);
    initDbJson(field, prop);
    initFormula(field, prop);
    initVersion(field, prop);
    initWhen(field, prop);
    initWhoProperties(field, prop);
    initDbMigration(field, prop);
    // Want to process last so we can use with @Formula
    if (field.has(Transient.class)) {
      // it is not a persistent property.
      prop.setDbRead(false);
      prop.setDbInsertable(false);
      prop.setDbUpdateable(false);
      prop.setTransient();
    }
    initEncrypt(field, prop);

    readPropGenValue(field, prop);

    for (Index index : annotationIndexes(field)) {
      addIndex(field, prop, index);
    }
  }

  public static Set<Index> annotationIndexes(XField field) {
    final Index ann = field.getAnnotation(Index.class);
    if (ann != null) {
      return Collections.singleton(ann);
    }
    final Indices collection = field.getAnnotation(Indices.class);
    if (collection != null) {
      Set<Index> result = new LinkedHashSet<>();
      Collections.addAll(result, collection.value());
      return result;
    }
    return Collections.emptySet();
  }

  private void initIdentity(XField field, DeployBeanProperty prop) {
    boolean id = prop.isId();
    GeneratedValue gen = field.getAnnotation(GeneratedValue.class);
    if (gen != null && id) {
      readGenValue(field, gen, prop);
    }
    if (id) {
      readIdScalar(prop);
    }
    Identity identity = field.getAnnotation(Identity.class);
    if (identity != null) {
      readIdentity(identity);
    }
    // determine the JDBC type using Lob/Temporal
    // otherwise based on the property Class
    Temporal temporal = field.getAnnotation(Temporal.class);
    if (temporal != null) {
      readTemporal(temporal, prop);
    } else if (field.has(Lob.class)) {
      util.setLobType(prop);
    }
    Length length = field.getAnnotation(Length.class);
    if (length != null) {
      prop.setDbLength(length.value());
    }
    if (field.has(NotNull.class)) {
      prop.setNullable(false);
    }
  }

  private void initValidation(XField field, DeployBeanProperty prop) {
    prop.setNullable(field.isNullable());
    if (!prop.isLob()) {
      Integer maxSize = field.getMaxLength();
      if (maxSize != null && maxSize > 0) {
        prop.setDbLength(maxSize);
      }
    }
  }

  private void initTenantId(XField field, DeployBeanProperty prop) {
    if (readConfig.checkValidationAnnotations()) {
      initValidation(field, prop);
    }
    if (field.has(TenantId.class)) {
      prop.setTenantId();
    }
    if (field.has(Draft.class)) {
      prop.setDraft();
    }
    if (field.has(DraftOnly.class)) {
      prop.setDraftOnly();
    }
    if (field.has(DraftDirty.class)) {
      prop.setDraftDirty();
    }
    if (field.has(DraftReset.class)) {
      prop.setDraftReset();
    }
    if (field.has(SoftDelete.class)) {
      prop.setSoftDelete();
    }
  }

  private void initDbJson(XField field, DeployBeanProperty prop) {
    DbComment comment = field.getAnnotation(DbComment.class);
    if (comment != null) {
      prop.setDbComment(comment.value());
    }
    DbMap dbMap = field.getAnnotation(DbMap.class);
    if (dbMap != null) {
      util.setDbMap(prop, dbMap);
      setColumnName(prop, dbMap.name());
    }
    DbJson dbJson = field.getAnnotation(DbJson.class);
    if (dbJson != null) {
      util.setDbJsonType(prop, dbJson);
      setColumnName(prop, dbJson.name());
    } else {
      DbJsonB dbJsonB = field.getAnnotation(DbJsonB.class);
      if (dbJsonB != null) {
        util.setDbJsonBType(prop, dbJsonB);
        setColumnName(prop, dbJsonB.name());
      }
    }
    DbArray dbArray = field.getAnnotation(DbArray.class);
    if (dbArray != null) {
      util.setDbArray(prop, dbArray);
      setColumnName(prop, dbArray.name());
    }
  }

  private void initFormula(XField field, DeployBeanProperty prop) {
    DocCode docCode = field.getAnnotation(DocCode.class);
    if (docCode != null) {
      prop.setDocCode(docCode);
    }
    DocSortable docSortable = field.getAnnotation(DocSortable.class);
    if (docSortable != null) {
      prop.setDocSortable(docSortable);
    }
    DocProperty docProperty = field.getAnnotation(DocProperty.class);
    if (docProperty != null) {
      prop.setDocProperty(docProperty);
    }
    Formula formula = getMetaAnnotationFormula(field, platform);
    if (formula != null) {
      prop.setSqlFormula(processFormula(formula.select()), processFormula(formula.join()));
    }
    final Aggregation aggregation = field.getAnnotation(Aggregation.class);
    if (aggregation != null) {
      prop.setAggregation(aggregation.value().replace("$1", prop.getName()));
    }
  }

  private Formula getMetaAnnotationFormula(XField field, Platform platform) {
    Formula fallback = null;
    for (Annotation ann : field.getAnnotations()) {
      if (ann.annotationType() == Formula.class) {
        Formula formula = (Formula) ann;
        final Platform[] platforms = formula.platforms();
        if (platforms.length == 0) {
          fallback = formula;
        } else if (matchPlatform(platforms, platform)) {
          return formula;
        }

      } else if (ann.annotationType() == Formula.List.class) {
        Formula.List formulaList = (Formula.List) ann;
        for (Formula formula : formulaList.value()) {
          final Platform[] platforms = formula.platforms();
          if (platforms.length == 0) {
            fallback = formula;
          } else if (matchPlatform(platforms, platform)) {
            return formula;
          }
        }
      }
    }
    return fallback;
  }

  private boolean matchPlatform(Platform[] platforms, Platform match) {
    for (Platform platform : platforms) {
      if (platform == match) {
        return true;
      }
    }
    return false;
  }

  private void initVersion(XField field, DeployBeanProperty prop) {
    if (field.has(Version.class)) {
      // explicitly specify a version column
      prop.setVersionColumn();
      generatedPropFactory.setVersion(prop);
    }
    Basic basic = field.getAnnotation(Basic.class);
    if (basic != null) {
      prop.setFetchType(basic.fetch());
      if (!basic.optional()) {
        prop.setNullable(false);
      }
    } else if (prop.isLob()) {
      // use the default Lob fetchType
      prop.setFetchType(defaultLobFetchType);
    }
  }

  private void initWhen(XField field, DeployBeanProperty prop) {
    if (field.has(WhenCreated.class)) {
      generatedPropFactory.setInsertTimestamp(prop);
    }
    if (field.has(WhenModified.class)) {
      generatedPropFactory.setUpdateTimestamp(prop);
    }
  }

  private void initEncrypt(XField field, DeployBeanProperty prop) {
    if (!prop.isTransient()) {
      EncryptDeploy encryptDeploy = util.encryptDeploy(info.getDescriptor().getBaseTableFull(), prop.getDbColumn());
      if (encryptDeploy == null || encryptDeploy.getMode() == Mode.MODE_ANNOTATION) {
        Encrypted encrypted = field.getAnnotation(Encrypted.class);
        if (encrypted != null) {
          setEncryption(prop, encrypted.dbEncryption(), encrypted.dbLength());
        }
      } else if (Mode.MODE_ENCRYPT == encryptDeploy.getMode()) {
        setEncryption(prop, encryptDeploy.isDbEncrypt(), encryptDeploy.getDbLength());
      }
    }
  }

  private void readIdentity(Identity identity) {
    descriptor.setIdentityMode(identity);
  }

  private void initDbMigration(XField field, DeployBeanProperty prop) {
    if (field.has(HistoryExclude.class)) {
      prop.setExcludedFromHistory();
    }
    DbDefault dbDefault = field.getAnnotation(DbDefault.class);
    if (dbDefault != null) {
      prop.setDbColumnDefault(dbDefault.value());
      if (prop.getGeneratedProperty() == null) {
        prop.setGeneratedProperty(new DefaultGeneratedProperty());
      }
    }

    Set<DbMigration> dbMigration = dbMigrations(field);
    dbMigration.forEach(ann -> prop.addDbMigrationInfo(
      new DbMigrationInfo(ann.preAdd(), ann.postAdd(), ann.preAlter(), ann.postAlter(), ann.platforms())));
  }

  public static Set<DbMigration> dbMigrations(XField field) {
    final DbMigration ann = field.getAnnotation(DbMigration.class);
    if (ann != null) {
      return Collections.singleton(ann);
    }
    final DbMigration.List collection = field.getAnnotation(DbMigration.List.class);
    if (collection != null) {
      Set<DbMigration> result = new LinkedHashSet<>();
      Collections.addAll(result, collection.value());
      return result;
    }
    return Collections.emptySet();
  }

  private void addIndex(XField field, DeployBeanProperty prop, Index index) {
    String[] columnNames;
    if (index.columnNames().length == 0) {
      columnNames = new String[]{prop.getDbColumn()};
    } else {
      columnNames = new String[index.columnNames().length];
      int i = 0;
      int found = 0;
      for (String colName : index.columnNames()) {
        if (colName.equals("${fa}") || colName.equals(prop.getDbColumn())) {
          columnNames[i++] = prop.getDbColumn();
          found++;
        } else {
          columnNames[i++] = colName;
        }
      }
      if (found != 1) {
        throw new RuntimeException("DB-columname has to be specified exactly one time in columnNames.");
      }
    }
    if (columnNames.length == 1 && hasRelationshipItem(field, prop)) {
      throw new RuntimeException("Can't use Index on foreign key relationships.");
    }
    descriptor.addIndex(new IndexDefinition(columnNames, index.name(), index.unique(), index.platforms(), index.concurrent(), index.definition()));
  }

  private void readJsonAnnotations(XField field, DeployBeanProperty prop) {
    if (jacksonAnnotationsPresent) {
      com.fasterxml.jackson.annotation.JsonIgnore jsonIgnore = field.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnore.class);
      if (jsonIgnore != null) {
        prop.setJsonSerialize(!jsonIgnore.value());
        prop.setJsonDeserialize(!jsonIgnore.value());
      }
    }
    Expose expose = field.getAnnotation(Expose.class);
    if (expose != null) {
      prop.setJsonSerialize(expose.serialize());
      prop.setJsonDeserialize(expose.deserialize());
    }
    JsonIgnore jsonIgnore = field.getAnnotation(JsonIgnore.class);
    if (jsonIgnore != null) {
      prop.setJsonSerialize(jsonIgnore.serialize());
      prop.setJsonDeserialize(jsonIgnore.deserialize());
    }
    if (field.has(UnmappedJson.class)) {
      prop.setUnmappedJson();
    }
  }

  private boolean hasRelationshipItem(XField field, DeployBeanProperty prop) {
    return field.has(OneToMany.class) || field.has(ManyToOne.class) || field.has(OneToOne.class);
  }

  private void setEncryption(DeployBeanProperty prop, boolean dbEncString, int dbLen) {
    util.checkEncryptKeyManagerDefined(prop.getFullBeanName());
    ScalarType<?> st = prop.getScalarType();
    if (byte[].class.equals(st.type())) {
      // Always using Java client encryption rather than DB for encryption
      // of binary data (partially as this is not supported on all db's etc)
      // This could be reviewed at a later stage.
      ScalarTypeBytesBase baseType = (ScalarTypeBytesBase) st;
      DataEncryptSupport support = createDataEncryptSupport(prop);
      ScalarTypeBytesEncrypted encryptedScalarType = new ScalarTypeBytesEncrypted(baseType, support);
      prop.setScalarType(encryptedScalarType);
      prop.setLocalEncrypted();
      return;
    }
    if (dbEncString) {
      DbEncrypt dbEncrypt = util.dbPlatform().dbEncrypt();
      if (dbEncrypt != null) {
        // check if we have a DB encryption function for this type
        int jdbcType = prop.getScalarType().jdbcType();
        DbEncryptFunction dbEncryptFunction = dbEncrypt.getDbEncryptFunction(jdbcType);
        if (dbEncryptFunction != null) {
          // Use DB functions to encrypt and decrypt
          prop.setDbEncryptFunction(dbEncryptFunction, dbEncrypt, dbLen);
          return;
        }
      }
    }
    prop.setScalarType(createScalarType(prop, st));
    prop.setLocalEncrypted();
    if (dbLen > 0) {
      prop.setDbLength(dbLen);
    }
  }

  @SuppressWarnings({"unchecked"})
  private ScalarTypeEncryptedWrapper<?> createScalarType(DeployBeanProperty prop, ScalarType<?> st) {
    // Use Java Encryptor wrapping the logical scalar type
    DataEncryptSupport support = createDataEncryptSupport(prop);
    ScalarTypeBytesBase byteType = getDbEncryptType(prop);
    return new ScalarTypeEncryptedWrapper(st, byteType, support);
  }

  private ScalarTypeBytesBase getDbEncryptType(DeployBeanProperty prop) {
    int dbType = prop.isLob() ? Types.BLOB : Types.VARBINARY;
    return (ScalarTypeBytesBase) util.typeManager().type(dbType);
  }

  private DataEncryptSupport createDataEncryptSupport(DeployBeanProperty prop) {
    String table = info.getDescriptor().getBaseTable();
    String column = prop.getDbColumn();
    return util.createDataEncryptSupport(table, column);
  }

  private void readGenValue(XField field, GeneratedValue gen, DeployBeanProperty prop) {
    descriptor.setIdGeneratedValue();
    SequenceGenerator seq = field.getAnnotation(SequenceGenerator.class);
    if (seq != null) {
      String seqName = seq.sequenceName();
      if (seqName.isEmpty()) {
        seqName = namingConvention.getSequenceName(descriptor.getBaseTable(), prop.getDbColumn());
      }
      descriptor.setIdentitySequence(seq.initialValue(), seq.allocationSize(), seqName);
    }

    GenerationType strategy = gen.strategy();
    if (strategy == GenerationType.IDENTITY) {
      descriptor.setIdentityType(IdType.IDENTITY);
    } else if (strategy == GenerationType.SEQUENCE) {
      descriptor.setIdentityType(IdType.SEQUENCE);
      if (!gen.generator().isEmpty()) {
        descriptor.setIdentitySequenceGenerator(gen.generator());
      }
    } else if (strategy == GenerationType.AUTO) {
      if (!gen.generator().isEmpty()) {
        // use a custom IdGenerator
        PlatformIdGenerator idGenerator = generatedPropFactory.getIdGenerator(gen.generator());
        if (idGenerator == null) {
          throw new IllegalStateException("No custom IdGenerator registered with name " + gen.generator());
        }
        descriptor.setCustomIdGenerator(idGenerator);
      } else if (prop.getPropertyType().equals(UUID.class)) {
        descriptor.setUuidGenerator();
      }
    }
  }

  private void readTemporal(Temporal temporal, DeployBeanProperty prop) {
    TemporalType type = temporal.value();
    if (type == TemporalType.DATE) {
      prop.setDbType(Types.DATE);
    } else if (type == TemporalType.TIMESTAMP) {
      prop.setDbType(Types.TIMESTAMP);
    } else if (type == TemporalType.TIME) {
      prop.setDbType(Types.TIME);
    } else {
      throw new PersistenceException("Unhandled type " + type);
    }
  }

}
