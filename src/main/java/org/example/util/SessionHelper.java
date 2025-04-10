package org.example.util;

import org.example.annotation.Aggregation;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class SessionHelper {

    /**
     * 设置自增主键值
     */
    public static <T> void setIdValue(T entity, Object idValue) throws IllegalAccessException {
        Field idField = EntityHelper.getIdField(entity.getClass());
        if (idField != null) {
            idField.setAccessible(true);
            // 如果是 Long 类型，直接赋值；如果是其他类型，转换后再赋值
            if (idField.getType() == Long.class) {
                idField.set(entity, idValue);
            } else {
                idField.set(entity, ((Number) idValue).longValue());
            }
        }
    }

    /**
     * 设置 INSERT 参数(单条插入)
     */
    public static void setInsertParameters(PreparedStatement stmt, Object entity) throws IllegalAccessException, SQLException {
        setInsertParameters(stmt, entity, 1);
    }
    /**
     * 设置 INSERT 参数(批量插入)
     */
    public static int setInsertParameters(PreparedStatement stmt, Object entity, int startIndex) throws IllegalAccessException, SQLException {
        Class<?> clazz = entity.getClass();
        List<Field> fields = EntityHelper.getInsertableFields(clazz);
        List<Field> filteredFields = fields.stream()
                .filter(f -> !f.isAnnotationPresent(Aggregation.class))
                .collect(Collectors.toList());

        int index = startIndex;
        for (Field field : filteredFields) {
            field.setAccessible(true);
            Object value = field.get(entity);
            stmt.setObject(index, value);
            index++;
        }
        return index;
    }

}
