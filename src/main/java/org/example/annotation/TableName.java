package org.example.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author duyujie
 * TableName 标记对应的表名
 * Target(ElementType.TYPE)             用于指定该自定义注解用于类、接口（包括注解类型）、枚举，取决于ElementType的值
 * Retention(RetentionPolicy.RUNTIME)   指定注解在运行时保留，并可以通过反射获取。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName {
    String value();
}
