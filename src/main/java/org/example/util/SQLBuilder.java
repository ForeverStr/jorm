package org.example.util;

import org.example.Enum.ErrorCode;
import org.example.annotation.Table;
import org.example.annotation.Column;
import org.example.annotation.Id;
import org.example.annotation.GeneratedValue;
import org.example.core.JormException;
import org.example.param.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SQLBuilder {
    private static final Logger log = LoggerFactory.getLogger(SQLBuilder.class);
    private static final Set<String> ALLOWED_ORDER_DIRECTIONS = Collections.unmodifiableSet(
            new HashSet<>(java.util.Arrays.asList("ASC", "DESC"))
    );
    // 生成 INSERT SQL
    public static String buildInsert(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name().isEmpty() ? clazz.getSimpleName().toLowerCase() : table.name();
        List<Field> fields = EntityHelper.getInsertableFields(clazz);

        String columns = fields.stream()
                //处理列表每个字段，如果设置了column的name就取该值，没有就取字段名
                .map(f -> {
                    Column column = f.getAnnotation(Column.class);
                    return (column != null && !column.name().isEmpty()) ? column.name() : f.getName();
                })
                //将所有列名通过逗号和空格连成字符串
                .collect(Collectors.joining(", "));
        log.info("fields：{}",fields);
        String placeholders = String.join(", ", Collections.nCopies(fields.size(), "?"));
        log.info("fields：{}",placeholders);
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
    // 生成 SELECT SQL 支持单表查询，条件，排序，条数限制，聚合函数。
    public static String buildFindSelect(Class<?> clazz, List<Condition> conditions,Integer limit,String orderBy,
                                         String group,List<Condition> havingConditions,String selectClause) {
        Table table = clazz.getAnnotation(Table.class);
        AssertUtils.throwAway(table, ErrorCode.SQL_GENERATION_FAILED);
        String tableName = !table.name().isEmpty() ? table.name() : clazz.getSimpleName().toLowerCase();
        List<String> validColumns = getValidColumns(clazz); // 通过反射获取有效列名
        // 创建一个 HashSet 并添加元素
        Set<String> tempSet = new HashSet<>(Arrays.asList("=", ">", "<", ">=", "<=", "LIKE"));
        // 将临时的 HashSet 转换为不可变的 Set
        Set<String> allowedOperators = Collections.unmodifiableSet(tempSet);
        for (Condition cond : conditions) {
            if (!validColumns.contains(cond.getColumn())) {
                throw new JormException(ErrorCode.INVALID_COLUMN);
            }
            if (!allowedOperators.contains(cond.getOperator().toUpperCase())) {
                throw new JormException(ErrorCode.INVALID_OPERATOR);
            }
        }
        //校验Select子句：
        if (!selectClause.equals("*") && !selectClause.trim().isEmpty()) {
            String[] selectParts = selectClause.split(",");
            for (String part : selectParts) {
                part = part.trim();
                // 匹配列名或聚合函数（如SUM(age)）
                if (!part.matches("[a-zA-Z_]+(\\s+AS\\s+[a-zA-Z_]+)?|([A-Z]+)\\([a-zA-Z_]+\\)(\\s+AS\\s+[a-zA-Z_]+)?")) {
                    throw new JormException(ErrorCode.INVALID_SELECT_CLAUSE);
                }
            }
        }
        StringBuilder sql = new StringBuilder("SELECT ").append(selectClause).append(" FROM ")
                .append(tableName);

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            List<String> conditionClauses = new ArrayList<>();
            for (Condition cond : conditions) {
                conditionClauses.add(cond.getColumn() + " " + cond.getOperator() + " ?");
            }
            sql.append(String.join(" AND ", conditionClauses));
        }
        if (group != null) {
            String[] groups = group.split(",");
            for (String g : groups) {
                if (!validColumns.contains(g.trim())) {
                    throw new JormException(ErrorCode.INVALID_COLUMN);
                }
            }
            sql.append(" GROUP BY ").append(String.join(", ", groups));
        }
        if (!havingConditions.isEmpty()) {
            sql.append(" HAVING ");
            List<String> clauses = new ArrayList<>();
            for (Condition cond : havingConditions) {
                clauses.add(cond.getColumn() + " " + cond.getOperator() + " ?");
            }
            sql.append(String.join(" AND ", clauses));
        }
        if (orderBy != null) {
            String[] orders = orderBy.split(",");
            for (String order : orders) {
                String[] parts = order.trim().split("\\s+");
                if (!validColumns.contains(parts[0])) {
                    throw new JormException(ErrorCode.INVALID_COLUMN);
                }
                if (parts.length > 1 && !ALLOWED_ORDER_DIRECTIONS.contains(parts[1].toUpperCase())) {
                    throw new JormException(ErrorCode.INVALID_ORDER_DIRECTION);
                }
            }
            sql.append(" ORDER BY ").append(orderBy);
        }
        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }
        log.info("{}",sql);
        return sql.toString();
    }
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

    // 生成 DELETE SQL（例如：DELETE FROM users WHERE id=?）
    public static String buildDelete(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name().isEmpty() ? clazz.getSimpleName().toLowerCase() : table.name();
        String idColumn = EntityHelper.getIdColumnName(clazz);
        return String.format("DELETE FROM %s WHERE %s=?", tableName, idColumn);
    }

    //列名白名单获取
    private static List<String> getValidColumns(Class<?> clazz) {
        List<String> columns = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Transient.class)) continue; // 忽略@Transient字段
            Column columnAnnotation = field.getAnnotation(Column.class);
            String columnName = (columnAnnotation != null && !columnAnnotation.name().isEmpty())
                    ? columnAnnotation.name()
                    : field.getName().toLowerCase();
            columns.add(columnName);
        }
        return columns;
    }
}
