package io.ebeaninternal.server.type;

import io.ebean.core.type.ScalarType;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ScalarTypeComputed {
  static Factory factory() {
    return new Factory();
  }

  static class Factory {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, ScalarType<?>> cache = new HashMap<>();

    public ScalarType<?> typeFor(Type valueType, boolean nullable) {
      lock.lock();
      try {
        String key = valueType + ":" + nullable;
        if (valueType.equals(String.class)) {
          return cache.computeIfAbsent(key, s -> ScalarTypeString.INSTANCE);
        }
        if (valueType.equals(Long.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeLong());
        }
        if (valueType.equals(Integer.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeInteger());
        }
        if (valueType.equals(Float.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeFloat());
        }
        if (valueType.equals(Double.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeDouble());
        }
        if (valueType.equals(BigDecimal.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeBigDecimal());
        }
        if (valueType.equals(BigInteger.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeMathBigInteger());
        }
        throw new IllegalArgumentException("Type [" + valueType + "] not supported for Computed mapping");
      } finally {
        lock.unlock();
      }
    }
  }
}
