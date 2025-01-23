package org.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author duyujie
 * TableName 标记对应的表名
 * FIELD     标记该注解只用于类的属性或成员变量
 */
//是否是主键
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableId {
    String value() default "id";
}