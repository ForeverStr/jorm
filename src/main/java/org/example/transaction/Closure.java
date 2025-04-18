package org.example.transaction;

import org.example.exception.ErrorCode;
import org.example.exception.JormException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.function.Consumer;

/**
 * <p>
 * 事务闭包，用于在事务中执行一些逻辑，并在事务结束后释放连接资源。
 * </p>
 * @author duyujie
 * @version 1.0
 */
public class Closure {
    private static final Logger log = LoggerFactory.getLogger(Closure.class);
    /**
     * <p>
     * 执行事务闭包，在事务中执行指定的逻辑，并在事务结束后释放连接资源。
     * </p>
     * @param txLogic 事务逻辑
     * @throws JormException 事务闭包异常 30005
     * @see TransactionManager
     */
    public static void transaction(Consumer<Connection> txLogic) {
        Connection conn = null;
        try {
            conn = TransactionManager.begin();
            txLogic.accept(conn);
            TransactionManager.commit();
        } catch (Exception e) {
            try {
                TransactionManager.rollback();
            } catch (Exception rollbackEx) {
                e.addSuppressed(rollbackEx); // 附加回滚异常
                log.error("事务回滚失败", rollbackEx);
            }
            throw new JormException(ErrorCode.TRANSACTION_CLOSURE_FAILED, e);
        } finally {
            if (conn != null) {
                try {
                    TransactionManager.release();
                } catch (Exception releaseEx) {
                    log.error("连接释放异常", releaseEx);
                }
            }
        }
    }
}
