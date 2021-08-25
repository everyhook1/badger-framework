package org.badger.tcc.entity;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Transactional
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Compensable {

    String identifier() default "";

    String tryMethod() default "";

    String confirmMethod() default "";

    String cancelMethod() default "";
}
