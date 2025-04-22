package io.github.foreverstr.session.base;

import io.github.foreverstr.core.DataSource;
import io.github.foreverstr.exception.ErrorCode;
import io.github.foreverstr.exception.JormException;
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

    // 用于支持手动事务，闭包事务
    protected BaseSession(Connection connection) {
        this.connection = connection;
        this.isManagedConnection = false;
    }

    // 支持自动事务
    protected BaseSession() {
        this(DataSource.getConnection());
        this.isManagedConnection = true;
        try {
            this.connection.setAutoCommit(true);
        } catch (SQLException e) {
            log.error("[ErrorCode={}] 自动事务开启失败，自动提交设置异常",
                    ErrorCode.TRANSACTION_AUTOMATIC_FAILED.getCode(), e);
            throw new JormException(ErrorCode.TRANSACTION_AUTOMATIC_FAILED, e);
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

    @Override
    public void close() {
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
            if (!connection.isClosed()) {
                if (!connection.getAutoCommit()) {
                    log.warn("未提交事务被自动回滚");
                    connection.rollback();
                }
                connection.close();
            }
        } catch (SQLException e) {
            closed.set(false);
            log.error("[ErrorCode={}] 关闭连接失败", ErrorCode.SESSION_CLOSED_FAILED.getCode(), e);
            throw new JormException(ErrorCode.SESSION_CLOSED_FAILED, e);
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
