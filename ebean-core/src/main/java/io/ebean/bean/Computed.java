package io.ebean.bean;

import io.ebean.common.ValueComputed;

public interface Computed<T> {
  boolean isReference();

  T get();

  static <A> Computed<A> of(A value) {
    return new ValueComputed<>(value);
  }
}
