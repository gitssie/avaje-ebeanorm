package org.tests.model.embedded;

import io.ebean.annotation.NotNull;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.InterceptReadWrite;

import jakarta.persistence.Embeddable;
import java.util.Date;

@Embeddable
public class EEmbDatePeriod implements EntityBean {
  public static final String[] _ebean_props = new String[]{"date1","date2"};
  @NotNull
  Date date1;
  @NotNull
  Date date2;

  private final EntityBeanIntercept _ebean_intercept;
  public EEmbDatePeriod() {
    this._ebean_intercept = new InterceptReadWrite(this);
  }

  public Date getDate1() {
    return date1;
  }

  public void setDate1(Date date1) {
    this.date1 = date1;
  }

  public Date getDate2() {
    return date2;
  }

  public void setDate2(Date date2) {
    this.date2 = date2;
  }

  @Override
  public String[] _ebean_getPropertyNames() {
    return _ebean_props;
  }

  @Override
  public String _ebean_getPropertyName(int pos) {
    return _ebean_props[pos];
  }

  @Override
  public Object _ebean_newInstance() {
    return new EEmbDatePeriod();
  }

  @Override
  public void _ebean_setEmbeddedLoaded() {
    System.out.println("xxx");


  }

  @Override
  public boolean _ebean_isEmbeddedNewOrDirty() {
    return false;
  }

  @Override
  public EntityBeanIntercept _ebean_getIntercept() {
    return _ebean_intercept;
  }

  @Override
  public EntityBeanIntercept _ebean_intercept() {
    return _ebean_intercept;
  }

  @Override
  public void _ebean_setField(int fieldIndex, Object value) {
    if(fieldIndex == 0){
      date1 = (Date) value;
    }else if(fieldIndex == 1){
      date2 = (Date) value;
    }
  }

  @Override
  public void _ebean_setFieldIntercept(int fieldIndex, Object value) {
    _ebean_setField(fieldIndex, value);
  }

  @Override
  public Object _ebean_getField(int fieldIndex) {
    if(fieldIndex == 0){
      return date1;
    }else if(fieldIndex == 1){
      return date2;
    }
    return null;
  }

  @Override
  public Object _ebean_getFieldIntercept(int fieldIndex) {
    return _ebean_getField(fieldIndex);
  }
}
