package io.github.foreverstr.session;

import io.github.foreverstr.cache.CacheManager;
import io.github.foreverstr.cache.SecondLevelCache;
import io.github.foreverstr.dto.Condition;
import io.github.foreverstr.session.base.BaseSession;
import io.github.foreverstr.sqlBuilder.DeleteBuilder;
import io.github.foreverstr.transaction.TransactionTemplate;
import io.github.foreverstr.util.EntityHelper;
import io.github.foreverstr.exception.ErrorCode;
import io.github.foreverstr.exception.JormException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>继承自BaseSession，实现对数据库的删除操作</p>
 * <p>所有操作均在事务中执行，事务提交失败时，会自动回滚。</p>
 * @author duyujie
 * @version 1.0
 * @see BaseSession
 * @see DeleteBuilder
 * @see EntityHelper
 */
public class DeleteSession extends BaseSession<DeleteSession> {
    private final List<Condition> conditions = new ArrayList<>();
    private final List<Object> params = new ArrayList<>();
    private int limit;
    private static final Logger log = LoggerFactory.getLogger(FindSession.class);
    public DeleteSession() {
        super();
    }
    public DeleteSession(Connection externalConn) {
        super(externalConn);
    }
    /**
     * 添加条件
     * @param column 条件列名
     * @param value 条件值
     * @return 当前对象 用于链式调用
     */
    public DeleteSession Where(String column, Object value) {
        conditions.add(new Condition(column, "=", value));
        params.add(value);
        return self();
    }
    /**
     * 添加limit限制
     * @param limit 限制数量
     * @return 当前对象 用于链式调用
     */
    public DeleteSession Limit(int limit) {
        this.limit = limit;
        return self();
    }

    /**
     * 删除单个实例对象或批量实例对象
     * @param entity 实例对象或实例对象集合
     * @param <T> 实体类型
     * @throws IllegalArgumentException 实体为null
     */
    public <T> void Delete(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("删除实体不能为null");
        }
        if (entity instanceof Collection) {
            deleteBatch((Collection<?>) entity);
        } else {
            deleteSingle(entity);
        }
        // 清除相关缓存
        if (CacheManager.isCacheEnabled()) {
            CacheManager.getSecondLevelCache().clearRegion(entity.getClass().getName());
            log.debug("清除缓存区域: [Class={}]", entity.getClass().getName());
        }
    }
    // 执行单个实例对象删除
    private <T> void deleteSingle(T entity) {
        checkIfClosed();

        Class<?> clazz = entity.getClass();
        String sql = null;
        try {
            sql = DeleteBuilder.buildSingleDelete(clazz);
            Object idValue = EntityHelper.getIdValue(entity);

            if (idValue == null) {
                throw new JormException(ErrorCode.INVALID_ENTITY, "实体ID值不能为null");
            }

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setObject(1, idValue);
                int rows = stmt.executeUpdate();
                log.debug("删除单个对象: [SQL={}, ID={}, 影响行数={}]", sql, idValue, rows);
            }
        } catch (IllegalAccessException e) {
            String errorMsg = String.format("反射访问失败 [类=%s]", clazz.getSimpleName());
            log.error("[ErrorCode={}] {}", ErrorCode.REFLECTION_ACCESS_FAILED.getCode(), errorMsg, e);
            throw new JormException(ErrorCode.REFLECTION_ACCESS_FAILED, errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = String.format("删除失败 [SQL=%s]", sql);
            log.error("[ErrorCode={}] {}", ErrorCode.DELETE_EXECUTION_FAILED.getCode(), errorMsg, e);
            throw new JormException(ErrorCode.DELETE_EXECUTION_FAILED, errorMsg, e);
        } finally {
            resetState();
        }
    }
    // 执行批量实例对象删除
    private <T> void deleteBatch(Collection<T> entities) {
        checkIfClosed();

        if (entities == null || entities.isEmpty()) {
            log.warn("批量删除实体集合为空");
            return;
        }
        List<T> entityList = new ArrayList<>(entities);
        Class<?> clazz = entityList.get(0).getClass();
        String sql = null;
        try {
            sql = DeleteBuilder.buildBatchDelete(entityList.get(0).getClass(), new ArrayList<>(entities));
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < entityList.size(); i++) {
                    Object idValue = EntityHelper.getIdValue(entityList.get(i));
                    if (idValue == null) {
                        throw new JormException(ErrorCode.INVALID_ENTITY, "实体ID值不能为null");
                    }
                    stmt.setObject(i + 1, idValue);
                }
                int rows = stmt.executeUpdate();
                log.debug("批量删除: [SQL={}, 数量={}, 影响行数={}]", sql, entityList.size(), rows);
            }
        } catch (IllegalAccessException e) {
            String errorMsg = String.format("反射访问失败 [类=%s]", clazz.getSimpleName());
            log.error("[ErrorCode={}] {}", ErrorCode.REFLECTION_ACCESS_FAILED.getCode(), errorMsg, e);
            throw new JormException(ErrorCode.REFLECTION_ACCESS_FAILED, errorMsg, e);
        }catch (SQLException e){
            String errorMsg = String.format("批量删除失败 [SQL=%s, 数量=%s]", sql, entityList.size());
            log.error("[ErrorCode={}] {}", ErrorCode.BATCH_DELETE_FAILED.getCode(), errorMsg, e);
            throw new JormException(ErrorCode.BATCH_DELETE_FAILED, errorMsg, e);
        }finally {
            // 每次执行后重置状态
            resetState();
        }
    }

    /**
     * 删除类对象 根据条件删除
     * @param clazz 类对象
     * @param <T> 实体类型
     * @throws IllegalArgumentException 类对象为null
     * @throws JormException 条件删除失败
     */
    public <T> void Delete(Class<T> clazz) {
        checkIfClosed();

        if (clazz == null) {
            throw new IllegalArgumentException("目标类不能为null");
        }

        String sql = null;
        try {
            sql = DeleteBuilder.buildClassDelete(clazz,conditions,limit);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    for (int i = 0; i < params.size(); i++) {
                        stmt.setObject(i + 1, params.get(i));
                    }
                int rows = stmt.executeUpdate();
                log.debug("条件删除: [SQL={}, 参数={}, 影响行数={}]", sql, params, rows);
            }
        } catch (SQLException e) {
            String errorMsg = String.format("条件删除失败 [SQL=%s, 参数=%s]", sql, params);
            log.error("[ErrorCode={}] {}", ErrorCode.CONDITIONAL_DELETE_FAILED.getCode(), errorMsg, e);
            throw new JormException(ErrorCode.CONDITIONAL_DELETE_FAILED, errorMsg, e);
        }finally {
            // 每次执行后重置状态
            resetState();
        }
        // 缓存清理
        if (CacheManager.isCacheEnabled()) {
            SecondLevelCache cache = CacheManager.getSecondLevelCache();
            final String regionToClear = clazz.getName();
            TransactionTemplate.doAfterCommit(() -> {
                cache.clearRegion(regionToClear);
                log.debug("Cleared cache region after commit: {}", regionToClear);
            });
        }
    }
    private void resetState() {
        this.conditions.clear();
         this.params.clear();
         this.limit = 0;
    }
    @Override
    protected DeleteSession self() {
        return this;
    }
}
