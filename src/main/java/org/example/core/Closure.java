package org.example.core;

import org.example.exception.ErrorCode;
import org.example.exception.JormException;

import java.sql.Connection;
import java.util.function.Consumer;

/**
 * 闭包事务
 */
public class Closure {
    public static void transaction(Consumer<Connection> txLogic) {
        Connection conn = TransactionManager.begin();
        try {
            txLogic.accept(conn);
            TransactionManager.commit();
        } catch (Exception e) {
            TransactionManager.rollback();
            throw new JormException(ErrorCode.TRANSACTION_FAILED, e);
        }
    }
}
