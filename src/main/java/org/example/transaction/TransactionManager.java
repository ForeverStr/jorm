package org.example.transaction;

import org.example.core.DataSource;
import org.example.exception.ErrorCode;
import org.example.exception.JormException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务管理器
 */
public class TransactionManager {
    // ThreadLocal保证线程安全
    private static final ThreadLocal<Connection> transactionConnectionHolder = new ThreadLocal<>();

    // 开启事务并返回连接
    public static Connection begin() {
        Connection conn = transactionConnectionHolder.get();
        if (conn != null) {
            throw new JormException(ErrorCode.NESTED_TRANSACTION_NOT_SUPPORTED);
        }
        conn = DataSource.getConnection();
        try {
            conn.setAutoCommit(false);
            transactionConnectionHolder.set(conn);
            return conn;
        } catch (SQLException e) {
            throw new JormException(ErrorCode.TRANSACTION_FAILED, e);
        }
    }

    // 提交事务
    public static void commit() {
        Connection conn = transactionConnectionHolder.get();
        if (conn == null) {
            throw new JormException(ErrorCode.NO_ACTIVE_TRANSACTION);
        }
        try {
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            throw new JormException(ErrorCode.COMMIT_FAILED, e);
        } finally {
            transactionConnectionHolder.remove();
        }
    }

    // 回滚事务
    public static void rollback() {
        Connection conn = transactionConnectionHolder.get();
        if (conn == null) return;
        try {
            conn.rollback();
            conn.close();
        } catch (SQLException e) {
            throw new JormException(ErrorCode.ROLLBACK_FAILED, e);
        } finally {
            transactionConnectionHolder.remove();
        }
    }

    // 获取当前事务连接（用于嵌套操作）
    public static Connection currentConnection() {
        return transactionConnectionHolder.get();
    }
}
