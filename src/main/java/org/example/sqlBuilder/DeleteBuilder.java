package org.example.sqlBuilder;

import org.example.annotation.Table;
import org.example.dto.Condition;
import org.example.util.EntityHelper;

import java.util.List;
import java.util.stream.Collectors;

public class DeleteBuilder {
    //单个实例删除语句
    public static String buildSingleDelete(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name().isEmpty() ?
                clazz.getSimpleName().toLowerCase() : table.name();
        StringBuilder whereClause = new StringBuilder();
        String idColumn = EntityHelper.getIdColumnName(clazz);
        whereClause.append(idColumn).append(" = ?");
        return String.format("DELETE FROM %s WHERE %s", tableName, whereClause);
    }
    //批量实例删除语句
    public static String buildBatchDelete(Class<?> clazz, List<Object> entities) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name().isEmpty() ?
                clazz.getSimpleName().toLowerCase() : table.name();
        StringBuilder whereClause = new StringBuilder();
        String idColumn = EntityHelper.getIdColumnName(clazz);
        whereClause.append(idColumn).append(" IN (");
        for (int i = 0; i < entities.size(); i++) {
            if (i > 0) {
                whereClause.append(",");
            }
            whereClause.append("?");
        }
        whereClause.append(")");
        return String.format("DELETE FROM %s WHERE %s", tableName, whereClause);
    }
    //类删除语句
    public static String buildClassDelete(Class<?> clazz, List<Condition> conditions, int limit) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name().isEmpty() ? clazz.getSimpleName().toLowerCase() : table.name();

        StringBuilder sql = new StringBuilder("DELETE FROM ").append(tableName);
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(conditions.stream()
                    .map(c -> c.getColumn() + " " + c.getOperator() + " ?")
                    .collect(Collectors.joining(" AND ")));
        }
        if (limit > 0) {
            sql.append(" LIMIT ").append(limit);
        }
        return sql.toString();
    }
}
