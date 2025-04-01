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

    /**
     * 单个增加
     */
    public static String buildInsert(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        AssertUtils.throwAway(table, ErrorCode.SQL_GENERATION_FAILED);
        String tableName = !table.name().isEmpty() ? table.name() : clazz.getSimpleName().toLowerCase();

        List<Field> fields = EntityHelper.getInsertableFields(clazz);

        List<Field> filteredFields = fields.stream()
                .filter(f ->!f.isAnnotationPresent(Aggregation.class))
                .collect(Collectors.toList());

        String columns = filteredFields.stream()
                .map(f -> {
                    Column column = f.getAnnotation(Column.class);
                    return (column != null && !column.name().isEmpty()) ? column.name() : f.getName();
                })
                .collect(Collectors.joining(", "));
        log.info("fields：{}",fields);

        String placeholders = String.join(", ", Collections.nCopies(filteredFields.size(), "?"));
        log.info("fields：{}",placeholders);
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
    }
}
