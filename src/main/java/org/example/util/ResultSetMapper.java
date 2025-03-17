package org.example.util;

import org.example.annotation.Column;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetMapper {

    public static <T> T mapToEntity(ResultSet rs, Class<T> clazz) throws SQLException, IllegalAccessException, InstantiationException {
        if (!rs.next()) return null;
        return mapRowToEntity(rs, clazz);
    }

    public static <T> List<T> mapToList(ResultSet rs, Class<T> clazz) throws SQLException, IllegalAccessException, InstantiationException {
        List<T> list = new ArrayList<>();
        while (rs.next()) {
            T entity = mapRowToEntity(rs, clazz);
            list.add(entity);
        }
        return list;
    }

    private static <T> T mapRowToEntity(ResultSet rs, Class<T> clazz) throws SQLException, IllegalAccessException, InstantiationException {
        T entity = clazz.newInstance();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Column column = field.getAnnotation(Column.class);
            String columnName = column != null && !column.name().isEmpty() ? column.name() : field.getName();
            Object value = rs.getObject(columnName);
            field.set(entity, value);
        }
        return entity;
    }
}
