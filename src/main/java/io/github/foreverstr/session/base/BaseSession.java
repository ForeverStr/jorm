package io.github.foreverstr.session.base;

import io.github.foreverstr.exception.ErrorCode;
import io.github.foreverstr.exception.JormException;
import io.github.foreverstr.session.factory.Jorm;
import io.github.foreverstr.transaction.CurrentTransactionConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>BaseSession 封装了数据库连接和自动事务管理，保存点管理。</p>
 * <p>子类需实现 {@link #self()} 方法，返回当前对象的引用，以支持链式调用。</p>
 * @author duyujie
 * @version 1.0
 * @param <T> 会话的具体类型，必须是 {@code BaseSession} 的子类，且泛型参数为自身类型。
 */
public abstract class BaseSession<T extends BaseSession<T>> implements AutoCloseable {
    protected Connection connection;
    protected boolean isManagedConnection;
    private final Map<String, Savepoint> savepoints = new LinkedHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(BaseSession.class);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Map<Class<?>, Map<Object, Object>> firstLevelCache = new HashMap<>(); // 一级缓存

    // 用于支持手动事务，闭包事务
    protected BaseSession(Connection connection) {
        this.connection = connection;
        this.isManagedConnection = false;
    }

    // 支持自动事务
    protected BaseSession() {
        this(Jorm.getConnection());
        this.isManagedConnection = true;
        // 只在当前线程没有事务时才启用自动提交
        if (!CurrentTransactionConnection.hasTransaction()) {
            try {
                this.connection.setAutoCommit(true);
            } catch (SQLException e) {
                log.error("[ErrorCode={}] 自动事务开启失败，自动提交设置异常",
                        ErrorCode.TRANSACTION_AUTOMATIC_FAILED.getCode(), e);
                throw new JormException(ErrorCode.TRANSACTION_AUTOMATIC_FAILED, e);
            }
        }
    }
    // 抽象方法：返回当前对象的引用（子类需实现）
    protected abstract T self();

    protected void checkIfClosed() {
        if (closed.get()) {
            log.error("当前会话已关闭");
            throw new JormException(ErrorCode.SESSION_HAS_CLOSED);
        }
    }
    protected <E> E getFromCache(Class<E> clazz, Object id) {
        Map<Object, Object> cachePerClass = firstLevelCache.get(clazz);
        if (cachePerClass != null) {
            return (E) cachePerClass.get(id);
        }
        return null;
    }

    protected void putInCache(Object entity, Object id) {
        Class<?> clazz = entity.getClass();
        Map<Object, Object> cachePerClass = firstLevelCache.computeIfAbsent(clazz, k -> new HashMap<>());
        cachePerClass.put(id, entity);
    }

    protected void clearCache() {
        firstLevelCache.clear();
    }


    @Override
    public void close() {
        clearCache(); // 清空一级缓存
        // 如果在事务中，不要关闭连接
        if (CurrentTransactionConnection.hasTransaction() &&
                CurrentTransactionConnection.get() == this.connection) {
            log.debug("Skipping connection close in transaction");
            return;
        }

        if (!closed.compareAndSet(false, true)) {
            return;
        }
        if (!isManagedConnection) {
            log.debug("非托管连接需手动管理，此处不执行关闭");
            return;
        }
        if (connection == null) {
            log.warn("连接已为空，无需关闭操作");
            return;
        }
        try {
            if (!connection.isClosed() && !connection.getAutoCommit()) {
                connection.rollback();
                log.debug("非事务环境：未提交事务已回滚");
            }
            if (!connection.isClosed()) connection.close();
        } catch (SQLException e) {
            log.error("[ErrorCode={}] 关闭连接失败", ErrorCode.SESSION_CLOSED_FAILED.getCode(), e);
            throw new JormException(ErrorCode.SESSION_CLOSED_FAILED, e);
        }finally {
            closed.set(true);
        }
    }
    /**
     * <p>
     * 创建保存点
     * </p>
     * @param name 保存点名称
     * @throws JormException 保存点创建失败或名称重复
     */
    public void createSavepoint(String name) {
        if (savepoints.containsKey(name)) {
            log.error("[ErrorCode={}] 保存点名称重复",
                    ErrorCode.DUPLICATE_SAVEPOINT_NAME.getCode());
            throw new JormException(ErrorCode.DUPLICATE_SAVEPOINT_NAME);
        }
        try {
            Savepoint sp = connection.setSavepoint(name);
            savepoints.put(name, sp);
        } catch (SQLException e) {
            log.error("[ErrorCode={}] 保存点创建失败: {}",
                    ErrorCode.SAVEPOINT_FAILED.getCode(), name, e);
            throw new JormException(ErrorCode.SAVEPOINT_FAILED, "保存点创建失败: " + name, e);
        }
    }
    /**
     * <p>
     * 回滚到保存点
     * </p>
     * @param name 需要回滚的保存点名称
     * @throws JormException 未找到保存点或回滚失败
     */
    public void rollbackToSavepoint(String name) {
        Savepoint sp = savepoints.get(name);
        if (sp == null) {
            log.error("[ErrorCode={}] 未找到保存点: {}",
                    ErrorCode.NO_SAVEPOINT.getCode(), name);
            throw new JormException(ErrorCode.NO_SAVEPOINT, "未找到保存点: " + name);
        }
        try {
            connection.rollback(sp);
            // 清理后续保存点
            List<String> toRemove = new ArrayList<>();
            boolean found = false;
            for (String key : savepoints.keySet()) {
                if (found) toRemove.add(key);
                if (key.equals(name)) found = true;
            }
            toRemove.forEach(savepoints::remove);
        } catch (SQLException e) {
            log.error("[ErrorCode={}] 回滚到保存点失败: {}",
                    ErrorCode.ROLLBACK_FAILED.getCode(), name, e);
            throw new JormException(ErrorCode.ROLLBACK_FAILED, "回滚到保存点失败: " + name, e);
        }
    }
    /**
     * <p>
     * 获取底层数据库连接
     * </p>
     * @return 底层数据库连接
     */
    public Connection getNativeConnection() {
        return connection;
    }
}
