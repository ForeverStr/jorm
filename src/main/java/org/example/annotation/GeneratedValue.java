package org.example.annotation;

import org.example.Enum.GenerationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @GeneratedValue 注解（主键生成策略）
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GeneratedValue {
    GenerationType strategy() default GenerationType.AUTO;
}
