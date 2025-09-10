package io.ebeaninternal.server.deploy.parse.tenant.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenericType {
}
