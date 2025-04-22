package io.github.foreverstr.sqlBuilder;

import io.github.foreverstr.dto.Condition;
import io.github.foreverstr.annotation.Table;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateBuilder {
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
