package io.github.foreverstr.transaction;

import io.github.foreverstr.session.factory.Jorm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * 事务模板类，提供简洁的编程式事务管理
 * 自动处理连接获取、事务提交/回滚和资源释放
 */
public class TransactionTemplate {
    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);
    private static final ThreadLocal<List<Runnable>> afterCommitCallbacks = new ThreadLocal<>();

    /**
     * 在事务中执行操作，无返回值
     */
    public void execute(Consumer<Connection> action) {
        execute(() -> {
            action.accept(null); // 参数不会被使用，实际连接从当前线程获取
            return null;
        });
    }

    /**
     * 在事务中执行操作，有返回值
     */
    public <T> T execute(Callable<T> action) {
        Connection conn = null;
        boolean existingTransaction = false;
        List<Runnable> callbacks = new ArrayList<>();
        afterCommitCallbacks.set(callbacks);

        try {
            // 检查是否已有事务
            conn = CurrentTransactionConnection.get();
            if (conn != null) {
                existingTransaction = true;
                log.debug("Using existing transaction");
            } else {
                // 创建新连接并开始事务
                conn = Jorm.getConnection();
                conn.setAutoCommit(false);
                CurrentTransactionConnection.set(conn);
                log.debug("Started new transaction");
            }

            // 执行用户代码
            T result = action.call();

            // 如果是新事务，则提交
            if (!existingTransaction) {
                conn.commit();
                log.debug("Transaction committed");

                // 执行提交后的回调
                callbacks = afterCommitCallbacks.get();
                if (callbacks != null) {
                    for (Runnable callback : callbacks) {
                        try {
                            callback.run();
                        } catch (Exception e) {
                            log.error("After-commit callback failed", e);
                        }
                    }
                }
            }

            return result;
        } catch (Exception e) {
            // 如果是新事务，则回滚
            if (!existingTransaction && conn != null) {
                try {
                    conn.rollback();
                    log.debug("Transaction rolled back due to exception", e);
                } catch (SQLException rollbackEx) {
                    log.warn("Rollback failed", rollbackEx);
                    e.addSuppressed(rollbackEx);
                }
            }

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException("Transaction execution failed", e);
            }
        } finally {
            // 清理资源
            if (!existingTransaction) {
                CurrentTransactionConnection.clear();
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        log.warn("Failed to close connection", e);
                    }
                }
            }
            afterCommitCallbacks.remove();
        }
    }

    /**
     * 注册事务提交后的回调
     */
    public static void doAfterCommit(Runnable callback) {
        List<Runnable> callbacks = afterCommitCallbacks.get();
        if (callbacks != null) {
            callbacks.add(callback);
        } else {
            // 不在事务中，立即执行
            callback.run();
        }
    }
}