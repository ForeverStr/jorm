package org.example.util;

import org.example.Enum.ErrorCode;
import org.example.annotation.Column;
import org.example.core.JormException;
import org.example.core.session.FindSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResultSetMapper {
    private static final Logger log = LoggerFactory.getLogger(ResultSetMapper.class);

    /**
     *
     * @param rs
     * @param clazz
     * @return T
     * @param <T>
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <T> T mapToEntity(ResultSet rs, Class<T> clazz)
            throws SQLException, IllegalAccessException, InstantiationException {
        if (!rs.next()) return null;
        return mapRowToEntity(rs, clazz);
    }

    /**
     * @param rs
     * @param clazz
     * @return List<T>
     * @param <T>
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <T> List<T> mapToList(ResultSet rs, Class<T> clazz)
            throws SQLException, IllegalAccessException, InstantiationException {
        List<T> list = new ArrayList<>();
        while (rs.next()) {
            T entity = mapRowToEntity(rs, clazz);
            list.add(entity);
        }
        return list;
    }
    /**
     * 统一处理结果集中的每一行数据
     */
    private static <T> T mapRowToEntity(ResultSet rs, Class<T> clazz)
            throws SQLException, IllegalAccessException, InstantiationException {
        T entity = clazz.newInstance();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Set<String> columnNames = new HashSet<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnLabel(i).toLowerCase());
        }
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                String columnName = !column.name().isEmpty() ? column.name() : field.getName();
                // log.info("列名：{}",columnName);
                // 检查列是否存在
                if (columnNames.contains(columnName.toLowerCase())) {
                    Object value = getValueByFieldType(rs,columnName,field.getType());
                    if (value != null) {
                        field.set(entity, value);
                    }
                }
            }
        }
        return entity;
    }
    /**
     * 根据字段类型从 ResultSet 中获取值
     */
    private static Object getValueByFieldType(ResultSet rs, String columnName, Class<?> fieldType)
            throws SQLException {
        try {
            if (fieldType == int.class || fieldType == Integer.class) {
                int value = rs.getInt(columnName);
                return rs.wasNull() ? (fieldType.isPrimitive() ? 0 : null) : value;
            } else if (fieldType == long.class || fieldType == Long.class) {
                long value = rs.getLong(columnName);
                return rs.wasNull() ? (fieldType.isPrimitive() ? 0L : null) : value;
            } else if (fieldType == String.class) {
                return rs.getString(columnName);
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                boolean value = rs.getBoolean(columnName);
                return rs.wasNull() ? (fieldType.isPrimitive() ? false : null) : value;
            } else {
                return rs.getObject(columnName);
            }
        } catch (SQLException e) {
            throw new JormException(
                    ErrorCode.TYPE_MISMATCH,
                    "Column '" + columnName + "' cannot be converted to type " + fieldType.getName(),
                    e
            );
        }
    }
}
