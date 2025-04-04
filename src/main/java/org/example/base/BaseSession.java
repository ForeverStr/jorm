package org.example.base;

import org.example.core.DataSource;
import org.example.exception.ErrorCode;
import org.example.exception.JormException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayDeque;
import java.util.Deque;

public abstract class BaseSession<T extends BaseSession<T>> implements AutoCloseable {
    protected Connection connection;
    protected boolean isManagedConnection; // 标记连接是否由Session管理
    private Deque<Savepoint> savepoints = new ArrayDeque<>();
    private static final Logger log = LoggerFactory.getLogger(BaseSession.class);

    // 支持显示事务
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
            throw new JormException(ErrorCode.CONNECTION_ERROR, e);
        }
    }

    // 用于显示开启事务
    public void beginTransaction() {
        try {
            if (connection.getAutoCommit()) {
                connection.setAutoCommit(false);
            } else {
                throw new JormException(ErrorCode.TRANSACTION_ALREADY_ACTIVE);
            }
        } catch (SQLException e) {
            throw new JormException(ErrorCode.TRANSACTION_FAILED, e);
        }
    }

    // 用于显示提交事务
    public void commit() {
        try {
            //检查事务提交状态
            if (!connection.getAutoCommit()) {
                connection.commit();
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new JormException(ErrorCode.COMMIT_FAILED, e);
        }
    }

    // 用于显示回滚事务
    public void rollback() {
        try {
            if (!connection.getAutoCommit()) {
                connection.rollback();
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new JormException(ErrorCode.ROLLBACK_FAILED, e);
        }
    }

    @Override
    public void close() {
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
    // 用于显示创建保存点
    public void createSavepoint(String name) {
        try {
            Savepoint sp = connection.setSavepoint(name);
            savepoints.push(sp);
        } catch (SQLException e) {
            throw new JormException(ErrorCode.SAVEPOINT_FAILED, e);
        }
    }
    // 用于显示回滚到保存点
    public void rollbackToSavepoint(String name) {
        if (savepoints.isEmpty()) {
            throw new JormException(ErrorCode.NO_SAVEPOINT);
        }
        Savepoint sp = savepoints.pop();
        try {
            connection.rollback(sp);
        } catch (SQLException e) {
            throw new JormException(ErrorCode.ROLLBACK_FAILED, e);
        }
    }
    // 获取原生连接（用于嵌套操作）
    public Connection getNativeConnection() {
        return connection;
    }
    // 抽象方法：返回当前对象的引用（子类需实现）
    protected abstract T self();
//    public T rollback() {
//        try {
//            connection.rollback();
//            transactionActive = false;
//        } catch (SQLException e) {
//            throw new RuntimeException("Rollback failed", e);
//        }
//        return self();
//    }
}
