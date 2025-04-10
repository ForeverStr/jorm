package org.example.sqlBuilder;

import org.example.annotation.Aggregation;
import org.example.annotation.Column;
import org.example.annotation.Table;
import org.example.exception.ErrorCode;
import org.example.util.AssertUtils;
import org.example.util.EntityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SaveBuilder {
    private static final Logger log = LoggerFactory.getLogger(SaveBuilder.class);
    // 表名获取方法
    private static String getTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        AssertUtils.throwAway(table, ErrorCode.SQL_GENERATION_FAILED);
        return !table.name().isEmpty() ? table.name() : clazz.getSimpleName().toLowerCase();
    }
    // 字段过滤方法
    private static List<Field> getInsertableFields(Class<?> clazz) {
        return EntityHelper.getInsertableFields(clazz).stream()
                .filter(f -> !f.isAnnotationPresent(Aggregation.class))
                .collect(Collectors.toList());
    }
    // 列名生成方法
    private static String generateColumnNames(List<Field> fields) {
        return fields.stream()
                .map(f -> {
                    Column column = f.getAnnotation(Column.class);
                    return (column != null && !column.name().isEmpty()) ? column.name() : f.getName();
                })
                .collect(Collectors.joining(", "));
    }

    // 单个插入
    public static String buildInsert(Class<?> clazz) {
        String tableName = getTableName(clazz);
        List<Field> fields = getInsertableFields(clazz);
        String columns = generateColumnNames(fields);
        String placeholders = String.join(", ", Collections.nCopies(fields.size(), "?"));

        log.info("Insert fields: {}", fields);
        log.info("Placeholders: {}", placeholders);

        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
    }

    // 批量插入
    public static String buildBatchInsert(Class<?> clazz, int batchSize) {
        String tableName = getTableName(clazz);
        List<Field> fields = getInsertableFields(clazz);
        String columns = generateColumnNames(fields);

        String singlePlaceholder = "(" + String.join(", ", Collections.nCopies(fields.size(), "?")) + ")";
        String allPlaceholders = String.join(", ", Collections.nCopies(batchSize, singlePlaceholder));

        return String.format("INSERT INTO %s (%s) VALUES %s", tableName, columns, allPlaceholders);
    }
}
