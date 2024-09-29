package io.ebean.typequery;

import io.avaje.lang.Nullable;
import io.ebean.ExpressionList;
import io.ebean.FetchConfig;
import io.ebean.FetchGroup;
import io.ebeaninternal.api.SpiQueryFetch;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.SpiFetchGroup;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Base type for associated beans.
 *
 * @param <T> the entity bean type (normal entity bean type e.g. Customer)
 * @param <R> the specific root query bean type (e.g. QCustomer)
 */
@SuppressWarnings("rawtypes")
public abstract class TQAssocBean<T, R> extends TQProperty<R, Object> {

  private static final FetchConfig FETCH_DEFAULT = FetchConfig.ofDefault();
  private static final FetchConfig FETCH_QUERY = FetchConfig.ofQuery();
  private static final FetchConfig FETCH_LAZY = FetchConfig.ofLazy();
  private static final FetchConfig FETCH_CACHE = FetchConfig.ofCache();

  /**
   * Construct with a property name and root instance.
   *
   * @param name the name of the property
   * @param root the root query bean instance
   */
  public TQAssocBean(String name, R root) {
    this(name, root, null);
  }

  /**
   * Construct with additional path prefix.
   */
  public TQAssocBean(String name, R root, String prefix) {
    super(name, root, prefix);
  }

  /**
   * Eagerly fetch this association fetching all the properties.
   */
  public final R fetch() {
    ((TQRootBean) _root).query().fetch(_name);
    return _root;
  }

  /**
   * Eagerly fetch this association using a "query join".
   */
  public final R fetchQuery() {
    ((TQRootBean) _root).query().fetchQuery(_name);
    return _root;
  }

  /**
   * Eagerly fetch this association using L2 bean cache.
   * Cache misses are populated via fetchQuery().
   */
  public final R fetchCache() {
    ((TQRootBean) _root).query().fetchCache(_name);
    return _root;
  }

  /**
   * Use lazy loading for fetching this association.
   */
  public final R fetchLazy() {
    ((TQRootBean) _root).query().fetchLazy(_name);
    return _root;
  }

  /**
   * Eagerly fetch this association with the properties specified.
   */
  public final R fetch(String properties) {
    ((TQRootBean) _root).query().fetch(_name, properties);
    return _root;
  }

  /**
   * Eagerly fetch this association using a "query join" with the properties specified.
   */
  public final R fetchQuery(String properties) {
    ((TQRootBean) _root).query().fetchQuery(_name, properties);
    return _root;
  }

  /**
   * Eagerly fetch this association using L2 cache with the properties specified.
   * Cache misses are populated via  fetchQuery().
   */
  public final R fetchCache(String properties) {
    ((TQRootBean) _root).query().fetchCache(_name, properties);
    return _root;
  }

  /**
   * Deprecated in favor of fetch().
   */
  @Deprecated
  public final R fetchAll() {
    return fetch();
  }

  /**
   * Eagerly fetch this association fetching some of the properties.
   */
  @SafeVarargs
  protected final R fetchProperties(TQProperty<?, ?>... props) {
    return fetchWithProperties(FETCH_DEFAULT, props);
  }

  /**
   * Eagerly fetch query this association fetching some of the properties.
   */
  @SafeVarargs
  protected final R fetchQueryProperties(TQProperty<?, ?>... props) {
    return fetchWithProperties(FETCH_QUERY, props);
  }

  /**
   * Eagerly fetch this association using L2 bean cache.
   */
  @SafeVarargs
  protected final R fetchCacheProperties(TQProperty<?, ?>... props) {
    return fetchWithProperties(FETCH_CACHE, props);
  }

  /**
   * Eagerly fetch query this association fetching some of the properties.
   */
  @SafeVarargs
  protected final R fetchLazyProperties(TQProperty<?, ?>... props) {
    return fetchWithProperties(FETCH_LAZY, props);
  }

  @SafeVarargs
  private R fetchWithProperties(FetchConfig config, TQProperty<?, ?>... props) {
    spiQuery().fetchProperties(_name, properties(props), config);
    return _root;
  }

  /**
   * Fetch using the nested FetchGroup.
   */
  public final R fetch(FetchGroup<T> nestedGroup) {
    return fetchNested(nestedGroup, FETCH_DEFAULT);
  }

  /**
   * Fetch query using the nested FetchGroup.
   */
  public final R fetchQuery(FetchGroup<T> nestedGroup) {
    return fetchNested(nestedGroup, FETCH_QUERY);
  }

  /**
   * Fetch cache using the nested FetchGroup.
   */
  public final R fetchCache(FetchGroup<T> nestedGroup) {
    return fetchNested(nestedGroup, FETCH_CACHE);
  }

  private R fetchNested(FetchGroup<T> nestedGroup, FetchConfig fetchConfig) {
    OrmQueryDetail nestedDetail = ((SpiFetchGroup) nestedGroup).underlying();
    spiQuery().addNested(_name, nestedDetail, fetchConfig);
    return _root;
  }

  private SpiQueryFetch spiQuery() {
    return (SpiQueryFetch) ((TQRootBean) _root).query();
  }

  @SafeVarargs
  private Set<String> properties(TQProperty<?, ?>... props) {
    Set<String> set = new LinkedHashSet<>();
    for (TQProperty<?, ?> prop : props) {
      set.add(prop.propertyName());
    }
    return set;
  }

  /**
   * Is equal to by ID property.
   */
  public final R eq(T other) {
    expr().eq(_name, other);
    return _root;
  }

  /**
   * Is EQUAL TO if value is non-null and otherwise no expression is added to the query.
   * <p>
   * This is effectively a helper method that allows a query to be built in fluid style where some predicates are
   * effectively optional. We can use <code>eqIfPresent()</code> rather than having a separate if block.
   */
  public final R eqIfPresent(@Nullable T other) {
    expr().eqIfPresent(_name, other);
    return _root;
  }

  /**
   * Is equal to by ID property.
   */
  public final R equalTo(T other) {
    return eq(other);
  }

  /**
   * Is not equal to by ID property.
   */
  public final R ne(T other) {
    expr().ne(_name, other);
    return _root;
  }

  /**
   * Is not equal to by ID property.
   */
  public final R notEqualTo(T other) {
    return ne(other);
  }

  /**
   * Is in a list of values.
   *
   * @param values the list of values for the predicate
   * @return the root query bean instance
   */
  @SafeVarargs
  public final R in(T... values) {
    expr().in(_name, (Object[]) values);
    return _root;
  }

  /**
   * Is in a list of values.
   *
   * @param values the list of values for the predicate
   * @return the root query bean instance
   */
  public final R in(Collection<T> values) {
    expr().in(_name, values);
    return _root;
  }

  /**
   * In where null or empty values means that no predicate is added to the query.
   * <p>
   * That is, only add the IN predicate if the values are not null or empty.
   * <p>
   * Without this we typically need to code an <code>if</code> block to only add
   * the IN predicate if the collection is not empty like:
   * </p>
   *
   * <h3>Without inOrEmpty()</h3>
   * <pre>{@code
   *
   *   List<String> names = Arrays.asList("foo", "bar");
   *
   *   QCustomer query = new QCustomer()
   *       .registered.before(LocalDate.now())
   *
   *   // conditionally add the IN expression to the query
   *   if (names != null && !names.isEmpty()) {
   *       query.name.in(names)
   *   }
   *
   *   query.findList();
   *
   * }</pre>
   *
   * <h3>Using inOrEmpty()</h3>
   * <pre>{@code
   *
   *   List<String> names = Arrays.asList("foo", "bar");
   *
   *   new QCustomer()
   *       .registered.before(LocalDate.now())
   *       .name.inOrEmpty(names)
   *       .findList();
   *
   * }</pre>
   */
  public final R inOrEmpty(Collection<T> values) {
    expr().inOrEmpty(_name, values);
    return _root;
  }

  /**
   * Is NOT in a list of values.
   *
   * @param values the list of values for the predicate
   * @return the root query bean instance
   */
  public final R notIn(Collection<T> values) {
    expr().notIn(_name, values);
    return _root;
  }

  /**
   * Is NOT in a list of values.
   *
   * @param values the list of values for the predicate
   * @return the root query bean instance
   */
  @SafeVarargs
  public final R notIn(T... values) {
    expr().notIn(_name, (Object[]) values);
    return _root;
  }

  /**
   * Apply a filter when fetching these beans.
   */
  public final R filterMany(ExpressionList<T> filter) {
    @SuppressWarnings("unchecked")
    ExpressionList<T> expressionList = (ExpressionList<T>) expr().filterMany(_name);
    expressionList.addAll(filter);
    return _root;
  }

  /**
   * Apply a filter when fetching these beans.
   * <p>
   * The expressions can use any valid Ebean expression and contain
   * placeholders for bind values using <code>?</code> or <code>?1</code> style.
   * </p>
   *
   * <pre>{@code
   *
   *     new QCustomer()
   *       .name.startsWith("Postgres")
   *       .contacts.filterMany("firstName istartsWith ?", "Rob")
   *       .findList();
   *
   * }</pre>
   *
   * <pre>{@code
   *
   *     new QCustomer()
   *       .name.startsWith("Postgres")
   *       .contacts.filterMany("whenCreated inRange ? to ?", startDate, endDate)
   *       .findList();
   *
   * }</pre>
   *
   * @param expressions The expressions including and, or, not etc with ? and ?1 bind params.
   * @param params      The bind parameter values
   */
  public final R filterMany(String expressions, Object... params) {
    expr().filterMany(_name, expressions, params);
    return _root;
  }

  /**
   * Is empty for a collection property.
   * <p>
   * This effectively adds a not exists sub-query on the collection property.
   * </p>
   * <p>
   * This expression only works on OneToMany and ManyToMany properties.
   * </p>
   */
  public final R isEmpty() {
    expr().isEmpty(_name);
    return _root;
  }

  /**
   * Is not empty for a collection property.
   * <p>
   * This effectively adds an exists sub-query on the collection property.
   * </p>
   * <p>
   * This expression only works on OneToMany and ManyToMany properties.
   * </p>
   */
  public final R isNotEmpty() {
    expr().isNotEmpty(_name);
    return _root;
  }

}
