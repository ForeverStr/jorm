package org.example.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public abstract class BaseSession<T extends BaseSession<T>> implements AutoCloseable {
    protected Connection connection;
    protected boolean transactionActive = false;
    private static final Logger log = LoggerFactory.getLogger(BaseSession.class);

    // 子类需通过构造函数初始化父类的connection
    protected BaseSession(Connection connection) {
        this.connection = connection;
    }

//    // 链式调用支持，where等
//    public T rollback() {
//        try {
//            connection.rollback();
//            transactionActive = false;
//        } catch (SQLException e) {
//            throw new RuntimeException("Rollback failed", e);
//        }
//        return self();
//    }

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
                log.error("关闭连接时发生 SQL 异常", e);
            }
        }
    }

    // 抽象方法：返回当前对象的引用（子类需实现）
    protected abstract T self();
}
