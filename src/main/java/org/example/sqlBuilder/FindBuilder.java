package org.example.sqlBuilder;

import org.example.annotation.Column;
import org.example.annotation.Table;
import org.example.dto.Condition;
import org.example.exception.ErrorCode;
import org.example.exception.JormException;
import org.example.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

public class FindBuilder {
    private static final Logger log = LoggerFactory.getLogger(FindBuilder.class);
    private static final Set<String> ALLOWED_ORDER_DIRECTIONS = Collections.unmodifiableSet(
            new HashSet<>(java.util.Arrays.asList("ASC", "DESC"))
    );
    /**
     * 单表查询 支持多WHERE，LIMIT，ORDER BY，GROUP BY，HAVING，SELECT
     */
    public static String buildFindSelect(Class<?> clazz, List<Condition> conditions,
                                         Integer limit,String orderBy,
                                         String group,List<Condition> havingConditions,
                                         String selectClause) {
        Table table = clazz.getAnnotation(Table.class);
        AssertUtils.throwAway(table, ErrorCode.SQL_GENERATION_FAILED);
        String tableName = !table.name().isEmpty() ? table.name() : clazz.getSimpleName().toLowerCase();

        List<String> validColumns = getValidColumns(clazz); // 通过反射获取有效列名

        Set<String> tempSet = new HashSet<>(Arrays.asList("=", ">", "<", ">=", "<=", "LIKE"));
        Set<String> allowedOperators = Collections.unmodifiableSet(tempSet);
        for (Condition cond : conditions) {
            if (!validColumns.contains(cond.getColumn())) {
                throw new JormException(ErrorCode.INVALID_COLUMN);
            }
            if (!allowedOperators.contains(cond.getOperator().toUpperCase())) {

                throw new JormException(ErrorCode.INVALID_OPERATOR);
            }
        }
        //校验Select子句
        if (!selectClause.equals("*") && !selectClause.trim().isEmpty()) {
            String[] selectParts = selectClause.split(",");
            for (String part : selectParts) {
                part = part.trim();
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

    /**
     * 列名白名单获取
     */
    private static List<String> getValidColumns(Class<?> clazz) {
        List<String> columns = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            // if (field.isAnnotationPresent(Aggregation.class)) continue;
            Column columnAnnotation = field.getAnnotation(Column.class);
            String columnName = (columnAnnotation != null && !columnAnnotation.name().isEmpty())
                    ? columnAnnotation.name()
                    : field.getName().toLowerCase();
            columns.add(columnName);
        }
        return columns;
    }
}
