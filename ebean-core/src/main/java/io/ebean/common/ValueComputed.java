package io.ebean.common;

import io.ebean.bean.Computed;

import java.io.Serializable;

public class ValueComputed<T> implements Computed<T>, Serializable {
  private static final long serialVersionUID = 3365725236140187588L;
  private T value;

  public ValueComputed(T value) {
    this.value = value;
  }

  @Override
  public boolean isReference() {
    return false;
  }

  @Override
  public T get() {
    return value;
  }

  @Override
  public String toString(){
    return value == null ? null : value.toString();
  }
}
