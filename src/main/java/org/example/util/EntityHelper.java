package org.example.util;

import org.example.annotation.Column;
import org.example.annotation.GeneratedValue;
import org.example.annotation.GenerationType;
import org.example.annotation.Id;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
public class EntityHelper {
    // 获取所有需要插入的字段（排除自增主键）
    public static List<Field> getInsertableFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> !f.isAnnotationPresent(Id.class) || f.getAnnotation(GeneratedValue.class).strategy() != GenerationType.IDENTITY)
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
                .orElseThrow(() -> new RuntimeException("No @Id field found in " + clazz.getName()));
    }
}
