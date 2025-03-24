package org.example.util;

import org.example.Enum.ErrorCode;
import org.example.core.JormException;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SessionHelper {

    // 工具方法：设置自增主键值
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

    // 工具方法：设置 INSERT 参数
    public static <T> void setInsertParameters(PreparedStatement stmt, T entity)
            throws SQLException, IllegalAccessException {
        Class<?> clazz = entity.getClass();
        List<Field> fields = EntityHelper.getInsertableFields(clazz);

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            field.setAccessible(true);  // 强制访问私有字段
            Object value = field.get(entity);
            stmt.setObject(i+1, value);
        }
    }

}
