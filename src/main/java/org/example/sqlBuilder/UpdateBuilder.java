package org.example.sqlBuilder;

import org.example.annotation.Column;
import org.example.annotation.Table;
import org.example.util.EntityHelper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateBuilder {
    // 生成 UPDATE SQL（例如：UPDATE users SET username=?, age=? WHERE id=?）
    public static String buildUpdate(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name().isEmpty() ? clazz.getSimpleName().toLowerCase() : table.name();
        List<Field> fields = EntityHelper.getUpdatableFields(clazz);
        String idColumn = EntityHelper.getIdColumnName(clazz);

        String setClause = fields.stream()
                .map(f -> {
                    Column column = f.getAnnotation(Column.class);
                    String columnName = column != null && !column.name().isEmpty() ? column.name() : f.getName();
                    return columnName + "=?";
                })
                .collect(Collectors.joining(", "));

        return String.format("UPDATE %s SET %s WHERE %s=?", tableName, setClause, idColumn);
    }
}
