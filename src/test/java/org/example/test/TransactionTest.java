package org.example.test;

import org.example.core.Closure;
import org.example.core.TransactionManager;
import org.example.entity.User;
import org.example.session.SaveSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

public class TransactionTest {

    //自动事务测试
    @Test
    void AutomaticTransactionTest() {
        try (SaveSession session = new SaveSession()) {
            User user = new User();
            user.setName("自动提交测试");
            session.save(user);
            Assertions.assertNotNull(user.getId());
        }
    }

    //手动事务测试
    @Test
    void ManualTransactionTest() {
        Connection conn = TransactionManager.begin();
        try (SaveSession session = new SaveSession(conn)) {
            User user = new User("手动事务测试", 22, "active");
            session.save(user);
            TransactionManager.commit();
        } catch (Exception e) {
            TransactionManager.rollback();
            throw e;
        }
    }

    //闭包事务测试；闭包事务测试
    @Test
    void  DeclarativeTransactionTest() {
        User user1 = new User("闭包事务测试1", 23, "active");
        User user2 = new User();
        Closure.transaction(conn -> {
            SaveSession session = new SaveSession(conn);
            session.save(user1);
            session.createSavepoint("user1保存点");
            try{
                user2.setAge(24);
                session.save(user2);
            }catch(Exception e){
                session.rollbackToSavepoint("user1保存点");
            }
        });
    }
}
