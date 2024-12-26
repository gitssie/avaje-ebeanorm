package io.ebeaninternal.server.deploy;

import io.ebean.bean.Computed;
import io.ebean.bean.EntityBean;
import io.ebean.common.ValueComputed;
import io.ebean.core.type.DataReader;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.bind.DataBind;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.query.SqlBeanLoad;

import javax.persistence.PersistenceException;
import java.sql.SQLException;

public class BeanPropertyComputed extends BeanProperty {
  private BeanComputedHelp computedHelp;
  private boolean isScalar = true;

  public BeanPropertyComputed(BeanDescriptor<?> descriptor, DeployBeanProperty deploy) {
    super(descriptor, deploy);
  }

  @Override
  public void initialise(BeanDescriptorInitContext initContext) {
    if (isAggregation() || isFormula()) {
      computedHelp = new BeanComputedHelp(name);
      isScalar = false;
    }
  }

  @Override
  public void loadIgnore(DbReadContext ctx) {
    if (isScalar) {
      super.loadIgnore(ctx);
    }
  }

  @Override
  public void load(SqlBeanLoad sqlBeanLoad) {
    if (isScalar) {
      super.load(sqlBeanLoad);
    }
  }

  @Override
  public boolean isComputed() {
    return true;
  }

  @Override
  public Object readSet(DataReader reader, EntityBean bean) throws SQLException {
    try {
      Object value = scalarType.read(reader);
      if (bean != null) {
        setValue(bean, new ValueComputed<>(value));
      }
      return value;
    } catch (Exception e) {
      throw new PersistenceException("Error readSet on " + descriptor + "." + name, e);
    }
  }

  @Override
  public void bind(DataBind b, Object value) throws SQLException {
    scalarType.bind(b, value == null ? null : ((Computed<?>) value).get());
  }

  @Override
  public <T> T createReference(EntityBean localBean, boolean forceNewReference) {
    if (computedHelp != null) {
      Computed<?> ref = computedHelp.createReference(localBean);
      setValue(localBean, ref);
      return (T) ref;
    }
    return null;
  }

  @Override
  public void setEbeanServer(SpiEbeanServer server) {
    if (computedHelp != null) {
      computedHelp.setLoader(server);
    }
  }
}
