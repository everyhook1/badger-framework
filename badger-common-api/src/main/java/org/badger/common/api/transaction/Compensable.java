package org.badger.common.api.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Compensable {

    String identifier() default "";

    String tryMethod() default "";

    String confirmMethod() default "";

    String cancelMethod() default "";
}
