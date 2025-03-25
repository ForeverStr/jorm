package org.example.util;

import org.example.exception.ErrorCode;
import org.example.Enum.TypeHandler;
import org.example.annotation.Column;
import org.example.exception.JormException;
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

    public static <T> T mapToEntity(ResultSet rs, Class<T> clazz)
            throws SQLException, IllegalAccessException, InstantiationException {
        if (!rs.next()) return null;
        return mapRowToEntity(rs, clazz);
    }
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
            String columnName = column == null ? field.getName()
                    : (!column.name().isEmpty() ? column.name() : field.getName());
            // log.info("列名：{}",columnName);
            if (columnNames.contains(columnName.toLowerCase())) {
                Object value = getValueByFieldType(rs,columnName,field.getType());
                if (value != null) {
                    field.set(entity, value);
                }
            }
        }
        return entity;
    }
    /**
     * 根据字段类型从 ResultSet 中获取值
     */
    private static Object getValueByFieldType(ResultSet rs, String columnName, Class<?> fieldType)
            throws JormException {
        try {
            TypeHandler handler = TypeHandler.forType(fieldType);
            if (handler != null) {
                return handler.handle(rs, columnName, fieldType);
            } else {
                return rs.getObject(columnName);
            }
        } catch (SQLException e) {
            throw new JormException(
                    ErrorCode.TYPE_MISMATCH,
                    String.format("Column '%s' (SQL type: %s) cannot map to Java type %s",
                            columnName,
                            getColumnTypeName(rs, columnName),
                            fieldType.getName()),
                    e
            );
        }
    }
    /**
     * 从 ResultSet 中获取指定列的 SQL 类型名称
     */
    private static String getColumnTypeName(ResultSet rs, String columnName) {
        try {
            int columnIndex = rs.findColumn(columnName);
            ResultSetMetaData metaData = rs.getMetaData();
            return metaData.getColumnTypeName(columnIndex);
        } catch (SQLException e) {
            return "UNKNOWN";
        }
    }
}
