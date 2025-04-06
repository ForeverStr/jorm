package org.example.base;

import org.example.core.DataSource;
import org.example.exception.ErrorCode;
import org.example.exception.JormException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Cleaner;
import java.sql.*;
import java.util.*;

public abstract class BaseSession<T extends BaseSession<T>> implements AutoCloseable {
    protected Connection connection;
    protected boolean isManagedConnection; // 标记连接是否由Session管理
    private final Map<String, Savepoint> savepoints = new LinkedHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(BaseSession.class);

    // 支持显示事务
    // 使用 Cleaner 检测未关闭的实例
    private static final Cleaner SESSION_CLEANER = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private static volatile boolean closed = false;

    protected BaseSession(Connection connection) {
        this.connection = connection;
        this.isManagedConnection = false;
        this.cleanable = SESSION_CLEANER.register(this, new CleanupAction(this.connection, this.isManagedConnection));
    }

    // 支持自动事务
    protected BaseSession() {
        this(DataSource.getConnection());
        this.isManagedConnection = true;
        try {
            this.connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new JormException(ErrorCode.CONNECTION_ERROR, e);
        }
    }
    // 抽象方法：返回当前对象的引用（子类需实现）
    protected abstract T self();

    protected void checkIfClosed() {
        if (closed) {
            throw new JormException(ErrorCode.SESSION_CLOSED);
        }
    }

    @Override
    public void close() {
        if (!closed){
            closed = true;
            cleanable.clean();
            if (isManagedConnection && connection != null) {
                try {
                    if (!connection.getAutoCommit()) {
                        log.warn("未提交的事务连接被关闭，自动回滚");
                        connection.rollback();
                    }
                    connection.close();
                } catch (SQLException e) {
                    log.error("关闭连接失败", e);
                }
            }
        }
    }
    // 用于显示创建保存点
    public void createSavepoint(String name) {
        if (savepoints.containsKey(name)) {
            throw new JormException(ErrorCode.DUPLICATE_SAVEPOINT_NAME);
        }
        try {
            Savepoint sp = connection.setSavepoint(name);
            savepoints.put(name, sp);
        } catch (SQLException e) {
            throw new JormException(ErrorCode.SAVEPOINT_FAILED, "保存点创建失败: " + name, e);
        }
    }
    // 用于显示回滚到保存点
    public void rollbackToSavepoint(String name) {
        Savepoint sp = savepoints.get(name);
        if (sp == null) {
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
            throw new JormException(ErrorCode.ROLLBACK_FAILED, "回滚到保存点失败: " + name, e);
        }
    }

    // 获取原生连接（用于嵌套操作）
    public Connection getNativeConnection() {
        return connection;
    }

    // 清理动作：当 Session 未被正确关闭时触发警告
    private static class CleanupAction implements Runnable {
        private final Connection connection;
        private final boolean isManagedConnection;

        CleanupAction(Connection connection, boolean isManagedConnection) {
            this.connection = connection;
            this.isManagedConnection = isManagedConnection;
        }

        @Override
        public void run() {
            if (isManagedConnection && connection != null) {
                // 记录严重错误（实际项目可通过日志框架输出）
                System.err.println("严重警告: 未使用 try-with-resources 关闭 Session！可能导致连接泄漏！");
                try {
                    if (!connection.isClosed()) {
                        connection.close(); // 强制关闭连接
                    }
                } catch (SQLException e) {
                    System.err.println("清理时关闭连接失败: " + e.getMessage());
                }
            }
        }
    }
}

//    public T rollback() {
//        try {
//            connection.rollback();
//            transactionActive = false;
//        } catch (SQLException e) {
//            throw new RuntimeException("Rollback failed", e);
//        }
//        return self();
//    }
