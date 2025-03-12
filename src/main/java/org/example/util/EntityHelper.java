package org.example.util;

import org.example.annotation.Column;
import org.example.annotation.GeneratedValue;
import org.example.Enum.GenerationType;
import org.example.annotation.Id;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
public class EntityHelper {
    // 获取需要插入的字段（排除自增主键）
    public static List<Field> getInsertableFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                //过滤字段
                .filter(f -> {
                    // 排除自增主键
                    if (f.isAnnotationPresent(Id.class)) {
                        GeneratedValue generatedValue = f.getAnnotation(GeneratedValue.class);
                        return generatedValue == null || generatedValue.strategy() != GenerationType.IDENTITY;
                    }
                    return true;
                })
                //收集非主键字段
                .collect(Collectors.toList());
    }

    // 获取主键字段名
    public static String getIdColumnName(Class<?> clazz) {
        Field idField = getIdField(clazz);
        Column column = idField.getAnnotation(Column.class);
        return column != null && !column.name().isEmpty() ? column.name() : idField.getName();
    }

    // 获取主键字段
    public static Field getIdField(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("该类没有主键字段" + clazz.getName()));
    }

    // 获取可更新字段（排除主键）
    public static List<Field> getUpdatableFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> !f.isAnnotationPresent(Id.class))
                .collect(Collectors.toList());
    }

    // 获取实体的主键值
    public static Object getIdValue(Object entity) throws IllegalAccessException {
        Field idField = getIdField(entity.getClass());
        idField.setAccessible(true);
        return idField.get(entity);
    }
}
