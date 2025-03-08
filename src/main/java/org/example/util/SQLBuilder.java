package org.example.util;

import org.example.annotation.Table;
import org.example.annotation.Column;
import org.example.annotation.Id;
import org.example.annotation.GeneratedValue;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SQLBuilder {
    // 生成 INSERT SQL（例如：INSERT INTO user (name, age) VALUES (?, ?)）
    public static String buildInsert(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name().isEmpty() ? clazz.getSimpleName().toLowerCase() : table.name();
        List<Field> fields = EntityHelper.getInsertableFields(clazz);

        // 修复 Stream 处理逻辑
        String columns = fields.stream()
                .filter(f -> f.getAnnotation(Column.class) != null)  // 过滤掉没有 @Column 注解的字段
                .map(f -> {
                    Column column = f.getAnnotation(Column.class);
                    // 处理列名：如果注解的 name 为空，则使用字段名
                    return column.name().isEmpty() ? f.getName() : column.name();
                })
                .collect(Collectors.joining(", "));

        String placeholders = String.join(", ", Collections.nCopies(fields.size(), "?"));
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
    }

    // 生成 SELECT BY ID SQL（例如：SELECT * FROM user WHERE id = ?）
    public static String buildSelectById(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name().isEmpty() ? clazz.getSimpleName().toLowerCase() : table.name();
        String idColumn = EntityHelper.getIdColumnName(clazz);
        return String.format("SELECT * FROM %s WHERE %s = ?", tableName, idColumn);
    }

    // 生成动态条件 SELECT SQL
    public static String buildSelect(Class<?> clazz, List<String> conditions, String orderBy, Integer limit, List<String> paramNames) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name().isEmpty() ? clazz.getSimpleName().toLowerCase() : table.name();
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);

        // 替换命名参数为 ?，并提取参数名
        List<String> processedConditions = new ArrayList<>();
        for (String condition : conditions) {
            Matcher matcher = Pattern.compile(":\\w+").matcher(condition);
            while (matcher.find()) {
                String paramName = matcher.group().substring(1); // 去掉冒号
                paramNames.add(paramName); // 记录参数名顺序
            }
            processedConditions.add(condition.replaceAll(":\\w+", "?")); // 替换为 ?
        }

        if (!processedConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", processedConditions));
        }
        if (orderBy != null) {
            sql.append(" ORDER BY ").append(orderBy);
        }
        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }
        return sql.toString();
    }
}
