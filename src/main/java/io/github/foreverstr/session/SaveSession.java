package io.github.foreverstr.session;

import io.github.foreverstr.annotation.Aggregation;
import io.github.foreverstr.annotation.Column;
import io.github.foreverstr.cache.CacheManager;
import io.github.foreverstr.session.base.BaseSession;
import io.github.foreverstr.exception.ErrorCode;
import io.github.foreverstr.exception.JormException;
import io.github.foreverstr.sqlBuilder.SaveBuilder;
import io.github.foreverstr.transaction.TransactionTemplate;
import io.github.foreverstr.util.SessionHelper;
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
 * <p>该类继承自 {@link BaseSession}，实现了增加逻辑的相关操作,必须通过 try-with-resources 使用本类</p>
 * @author duyujie
 * @version 1.0
 * @see BaseSession
 * @see SaveBuilder
 * @see SessionHelper
 */
public class SaveSession extends BaseSession<SaveSession> {
    // 缓存非空字段校验信息
    private static final Map<Class<?>, List<Field>> nonNullableFieldsCache = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(SaveSession.class);
    public SaveSession() {
        super();
    }
    public SaveSession(Connection externalConn) {
        super(externalConn);
    }

    /**
     * <p>单个增加</p>
     * <p>不存在于数据库表但实际业务需要的字段需要加上{@link Aggregation}</p>
     * @param <T> 泛型参数，表示要保存的实体对象的类型。
     * @param entity 实体对象
     * @throws JormException 数据库操作异常，包括主键冲突，SQL生成失败，SQL执行失败，参数绑定失败
     */
    public <T> void save(T entity) {
        checkIfClosed();
        try {
            String sql;
            try {
                sql = SaveBuilder.buildInsert(entity.getClass());
            } catch (Exception e) {
                log.error("单个插入SQL生成失败: {}", e.getMessage(), e);
                throw new JormException(ErrorCode.SQL_GENERATION_FAILED);
            }
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                SessionHelper.setInsertParameters(stmt, entity);
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    SessionHelper.setIdValue(entity, rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                log.error("单个插入数据主键冲突: {}", e.getMessage(), e);
                throw new JormException(ErrorCode.DUPLICATE_KEY);
            } else {
                log.error("单个插入SQL执行失败: SQL State={}, Error Code={}, Message={}",
                        e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
                throw new JormException(ErrorCode.SQL_EXECUTION_FAILED, e);
            }
        } catch (IllegalAccessException e) {
            log.error("单个插入参数绑定失败: {}", e.getMessage(), e);
            throw new JormException(ErrorCode.PARAMETER_BINDING_FAILED);
        }
        // 清除相关缓存
        if (CacheManager.isCacheEnabled()) {
            final String regionToClear = entity.getClass().getName();
            TransactionTemplate.doAfterCommit(() -> {
                CacheManager.getSecondLevelCache().clearRegion(regionToClear);
                log.debug("Cleared cache region after commit: {}", regionToClear);
            });
        }
    }
    /**
     * <p>批量增加</p>
     * <p>不存在于数据库表但实际业务需要的字段需要加上{@link Aggregation}</p>
     * @param <T> 泛型参数
     * @param entities 实体对象列表
     * @return 新增记录的主键列表
     * @throws JormException 数据库操作异常，包括主键冲突，SQL生成失败，SQL执行失败，参数绑定失败
     */
    public <T> List<Long> batchSave(List<T> entities) {
        if (entities.isEmpty()) {
            return Collections.emptyList();
        }
        for (T entity : entities) {
            validateEntity(entity);
        }
        try {
            String sql;
            try{
                sql = SaveBuilder.buildBatchInsert(entities.get(0).getClass(), entities.size());
            } catch (Exception e) {
                log.error("批量插入SQL生成失败: {}", e.getMessage(), e);
                throw new JormException(ErrorCode.SQL_GENERATION_FAILED);
            }
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
            if (e.getSQLState().equals("23505")) {
                log.error("批量插入数据主键冲突: {}", e.getMessage(), e);
                throw new JormException(ErrorCode.DUPLICATE_KEY);
            } else {
                log.error("批量插入SQL执行失败: SQL State={}, Error Code={}, Message={}",
                        e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
                throw new JormException(ErrorCode.SQL_EXECUTION_FAILED, e);
            }
        } catch (IllegalAccessException e) {
            log.error("批量插入参数绑定失败: {}", e.getMessage(), e);
            throw new JormException(ErrorCode.PARAMETER_BINDING_FAILED);
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
                    log.error("字段 {} 不能为空", field.getName());
                    throw new JormException(ErrorCode.INVALID_COLUMN);
                }
            } catch (IllegalAccessException e) {
                log.error("字段 {} 访问失败", field.getName());
                throw new JormException(ErrorCode.TYPE_MISMATCH);
            }
        }
    }
    @Override
    protected SaveSession self() {
        return this;
    }
}
