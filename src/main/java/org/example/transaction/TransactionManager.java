package org.example.transaction;

import org.example.core.DataSource;
import org.example.exception.ErrorCode;
import org.example.exception.JormException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * <p>事务管理器</p>
 * <p>
 * TransactionManager用于处理手动事务和闭包事务，需要用户手动开启事务、提交事务、回滚事务、释放连接。
 * </p>
 * <p>
 * 示例代码如下：<pre>        Connection connection = TransactionManager.begin();
 *         try (JormSession session = new JormSession(connection)) {
 *             ...
 *             TransactionManager.commit();
 *             ...
 *         }catch (Exception e){
 *             TransactionManager.rollback();
 *             throw ...
 *         }finally {
 *             TransactionManager.release();
 *         }</pre>
 * </p>
 * @author duyujie
 * @version 1.0
 */
public class TransactionManager {
    // ThreadLocal保证线程安全
    private static final ThreadLocal<Connection> transactionConnectionHolder = new ThreadLocal<>();
    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    /**
     * <p>开启事务</p>
     * <p>
     *     示例代码如下：<pre>        Connection connection = TransactionManager.begin();</pre>
     * </p>
     * @return 事务连接
     * @throws JormException 事务异常
     */
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
            log.error("[ErrorCode={}] 事务开启失败", ErrorCode.TRANSACTION_BEGIN_FAILED.getCode(), e);
            throw new JormException(ErrorCode.TRANSACTION_BEGIN_FAILED, e);
        }
    }

    /**
     * <p>提交事务</p>
     * <p>
     *     示例代码如下：<pre>        TransactionManager.commit();</pre>
     * </p>
     * @throws JormException 事务异常
     */
    public static void commit() {
        Connection conn = transactionConnectionHolder.get();
        if (conn == null) {
            log.warn("连接已从线程变量中移除，无法提交事务");
            throw new JormException(ErrorCode.NO_ACTIVE_CONNECTION);
        }
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new JormException(ErrorCode.COMMIT_FAILED, e);
        }
    }

    /**
     * <p>回滚事务</p>
     * <p>
     *     示例代码如下：<pre>        TransactionManager.rollback();</pre>
     * </p>
     * @throws JormException 事务异常
     */
    public static void rollback() {
        Connection conn = transactionConnectionHolder.get();
        if (conn == null) return;
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new JormException(ErrorCode.ROLLBACK_FAILED, e);
        }
    }

    /**
     * <p>释放连接</p>
     * <p>
     *     示例代码如下：<pre>        TransactionManager.release();</pre>
     * </p>
     * @throws JormException 事务异常
     */
    public static void release() {
        Connection conn = transactionConnectionHolder.get();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("关闭连接失败", e);
                throw new JormException(ErrorCode.CONNECTION_ERROR, e);
            } finally {
                transactionConnectionHolder.remove();
            }
        }
    }

    // 获取当前事务连接（用于嵌套操作）
    public static Connection currentConnection() {
        return transactionConnectionHolder.get();
    }
}
