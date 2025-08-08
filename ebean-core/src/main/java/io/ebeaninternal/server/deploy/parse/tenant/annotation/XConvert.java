package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import javax.persistence.Convert;
import java.lang.annotation.Annotation;

public class XConvert implements Convert {
  private Class converter;
  private String attributeName = "";
  private boolean disableConversion = false;

  public XConvert() {
  }

  public XConvert(Class converter) {
    this.converter = converter;
  }

  @Override
  public Class converter() {
    return converter;
  }

  @Override
  public String attributeName() {
    return attributeName;
  }

  @Override
  public boolean disableConversion() {
    return disableConversion;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return Convert.class;
  }

  public Class getConverter() {
    return converter;
  }

  public void setConverter(Class converter) {
    this.converter = converter;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public boolean isDisableConversion() {
    return disableConversion;
  }

  public void setDisableConversion(boolean disableConversion) {
    this.disableConversion = disableConversion;
  }
}
