package org.example.util;

import org.example.annotation.Aggregation;
import org.example.annotation.Column;
import org.example.annotation.Enum.GenerationType;
import org.example.annotation.GeneratedValue;
import org.example.annotation.Id;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public class EntityHelper {
    /**
     * 获取需要插入的字段（排除自增主键和聚合字段）
     */
    public static List<Field> getInsertableFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> {
                    if (f.isAnnotationPresent(Id.class)) {
                        GeneratedValue generatedValue = f.getAnnotation(GeneratedValue.class);
                        return generatedValue == null || generatedValue.strategy() != GenerationType.IDENTITY;
                    }
                    return !f.isAnnotationPresent(Aggregation.class);
                })
                .collect(Collectors.toList());
    }
    /**
     * 获取主键字段名
     */
    public static String getIdColumnName(Class<?> clazz) {
        Field idField = getIdField(clazz);
        Column column = idField.getAnnotation(Column.class);
        return column != null && !column.name().isEmpty() ? column.name() : idField.getName();
    }

    /**
     * 获取主键字段
     */
    public static Field getIdField(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("该类没有主键字段" + clazz.getName()));
    }

    /**
     * 获取可更新字段（排除主键）
     */
    public static List<Field> getUpdatableFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> !f.isAnnotationPresent(Id.class))
                .collect(Collectors.toList());
    }

    /**
     * 获取实体的主键值
     */
    public static Object getIdValue(Object entity) throws IllegalAccessException {
        Field idField = getIdField(entity.getClass());
        idField.setAccessible(true);
        return idField.get(entity);
    }
    // 获取非空字段（排除主键）
    public static Map<String, Object> getNonNullFields(Object entity) {
        Map<String, Object> fields = new LinkedHashMap<>();
        Class<?> clazz = entity.getClass();
        Field idField = getIdField(clazz);

        for (Field field : getUpdatableFields(clazz)) {
            if (field.equals(idField)) continue;

            try {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value != null) {
                    String column = getColumnName(field);
                    fields.put(column, value);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Field access failed", e);
            }
        }
        return fields;
    }
    private static String getColumnName(Field field) {
        Column columnAnno = field.getAnnotation(Column.class);
        return (columnAnno != null && !columnAnno.name().isEmpty())
                ? columnAnno.name()
                : field.getName();
    }
}
