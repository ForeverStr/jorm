package org.example.session;

import org.example.annotation.Column;
import org.example.annotation.RequireTryWithResources;
import org.example.base.BaseSession;
import org.example.core.DataSource;
import org.example.exception.ErrorCode;
import org.example.exception.JormException;
import org.example.sqlBuilder.SaveBuilder;
import org.example.util.SessionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 必须通过 try-with-resources 使用本类，例如：
 * try (SaveSession session = new SaveSession()) {
 *     session.save(entity);
 * }
 */
public class SaveSession extends BaseSession<SaveSession> {
    // 缓存非空字段校验信息
    private static final Map<Class<?>, List<Field>> nonNullableFieldsCache = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(SaveSession.class);
    // 自动事务模式
    public SaveSession() {
        super();
    }
    // 显示事务模式
    public SaveSession(Connection externalConn) {
        super(externalConn);
    }

    /**
     * 单个增加
     * @param entity
     * @param <T>
     */
    public <T> void save(T entity) {
        checkIfClosed();
        try {
            String sql = SaveBuilder.buildInsert(entity.getClass());
            log.info("预编译前的sql：{}", sql);
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                log.info("预编译后的sql：{}", stmt);
                SessionHelper.setInsertParameters(stmt, entity);
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    SessionHelper.setIdValue(entity, rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new JormException(ErrorCode.SQL_EXECUTION_FAILED, e);
        } catch (IllegalAccessException e) {
            throw new JormException(ErrorCode.PARAMETER_BINDING_FAILED, e);
        }
    }
    /**
     * 批量增加
     * @param entities
     * @return
     * @param <T>
     */
    public <T> List<Long> batchSave(List<T> entities) {
        if (entities.isEmpty()) {
            return Collections.emptyList();
        }

        for (T entity : entities) {
            validateEntity(entity);
        }

        try {
            String sql = SaveBuilder.buildBatchInsert(entities.get(0).getClass(), entities.size());
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int paramIndex = 1;
                for (T entity : entities) {
                    paramIndex = SessionHelper.setInsertParameters(stmt, entity, paramIndex);
                }
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                List<Long> ids = new ArrayList<>();
                while (rs.next()) {
                    ids.add(rs.getLong(1));
                }
                return ids;
            }
        } catch (SQLException e) {
            throw new JormException(ErrorCode.SQL_EXECUTION_FAILED, e);
        } catch (IllegalAccessException e) {
            throw new JormException(ErrorCode.PARAMETER_BINDING_FAILED, e);
        }
    }

    // 遍历所有字段，检查 @Column(nullable = false) 的字段是否非空
    private <T> void validateEntity(T entity) {
        Class<?> clazz = entity.getClass();
        List<Field> nonNullableFields = nonNullableFieldsCache.computeIfAbsent(clazz, k -> {
            List<Field> fields = new ArrayList<>();
            for (Field field : clazz.getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                if (column != null && !column.nullable()) {
                    fields.add(field);
                }
            }
            return fields;
        });

        for (Field field : nonNullableFields) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value == null) {
                    throw new JormException(ErrorCode.INVALID_COLUMN, "字段 " + field.getName() + " 不能为空");
                }
            } catch (IllegalAccessException e) {
                throw new JormException(ErrorCode.TYPE_MISMATCH, e);
            }
        }
    }
    @Override
    protected SaveSession self() {
        return this;
    }
}
