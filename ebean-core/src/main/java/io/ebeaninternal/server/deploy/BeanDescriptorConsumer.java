package io.ebeaninternal.server.deploy;

import java.util.function.Consumer;

public class BeanDescriptorConsumer<T> {
  private Class<?> beanClass;
  private Class<?> targetClass;
  private Consumer<BeanDescriptor<T>> consumer;

  public BeanDescriptorConsumer(Class<?> beanClass, Class<T> targetClass, Consumer<BeanDescriptor<T>> consumer) {
    this.beanClass = beanClass;
    this.targetClass = targetClass;
    this.consumer = consumer;
  }

  public boolean isOwner(Class<?> beanClass) {
    return this.beanClass.equals(beanClass);
  }

  public void callback(BeanDescriptor<T> desc) {
    if (this.targetClass.equals(desc.type())) {
      this.consumer.accept(desc);
    }
  }
}
