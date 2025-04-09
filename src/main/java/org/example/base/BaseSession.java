package org.example.base;

import org.example.core.DataSource;
import org.example.exception.ErrorCode;
import org.example.exception.JormException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
public abstract class BaseSession<T extends BaseSession<T>> implements AutoCloseable {
    protected Connection connection;
    protected boolean isManagedConnection;
    private final Map<String, Savepoint> savepoints = new LinkedHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(BaseSession.class);
    private volatile boolean closed = false;

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

    public Connection getNativeConnection() {
        return connection;
    }
}
