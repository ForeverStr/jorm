package org.example.sqlBuilder;

import org.example.annotation.Column;
import org.example.annotation.Table;
import org.example.dto.Condition;
import org.example.session.UpdateSession;
import org.example.util.EntityHelper;

import java.lang.reflect.Field;
import java.security.PublicKey;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    // 构建根据指定条件更新SQL
    public static String buildUpdateSql(Class<?> clazz ,
                                        List<Condition> conditions,
                                        Map<String, Object> updates) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name().isEmpty()
                        ? clazz.getSimpleName().toLowerCase() : table.name();

        String setClause = updates.keySet().stream()
                .map(column -> column + "=?")
                .collect(Collectors.joining(", "));
        String whereClause = conditions.stream()
                .map(condition -> condition.getColumn() + " " + condition.getOperator() + " ?")
                .collect(Collectors.joining(" AND "));
        return String.format("UPDATE %s SET %s WHERE %s", tableName, setClause, whereClause);
    }
}
