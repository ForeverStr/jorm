package org.example.base;

import java.sql.*;
public abstract class BaseSession<T extends BaseSession<T>> implements AutoCloseable {
    protected Connection connection;
    protected boolean transactionActive = false;

    // 子类需通过构造函数初始化 connection
    protected BaseSession(Connection connection) {
        this.connection = connection;
    }

    // 开启事务（返回当前对象以支持链式调用）
    public void beginTransaction() {
        try {
            connection.setAutoCommit(false);
            transactionActive = true;
        } catch (SQLException e) {
            throw new RuntimeException("Begin transaction failed", e);
        }
    }

    // 提交事务
    public void commit() {
        try {
            connection.commit();
            transactionActive = false;
        } catch (SQLException e) {
            throw new RuntimeException("Commit failed", e);
        }
    }

    // 回滚事务
    public void rollback() {
        try {
            connection.rollback();
            transactionActive = false;
        } catch (SQLException e) {
            throw new RuntimeException("Rollback failed", e);
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                if (transactionActive) {
                    rollback(); // 自动回滚未提交的事务
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 抽象方法：返回当前对象的引用（子类需实现）
    protected abstract T self();
}
