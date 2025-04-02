package org.example.test;

import org.example.core.JormSession;
import org.example.core.TransactionManager;
import org.example.entity.User;
import org.example.session.SaveSession;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SaveTest {
    //保存测试
    @Test
    void testAutoCommit() {
        // 默认自动提交（不需要try-with-resources）
        User user1 = new User();
        user1.setName("自动提交测试1");
        User user2 = new User();
        user2.setName("自动提交测试2");
        new SaveSession().save(user1);
        new SaveSession().save(user2);
        System.out.println(user1.getId());
        System.out.println(user2.getId());
    }
    @Test
    void testManualTransaction() {
        Connection conn = TransactionManager.begin();
        try (SaveSession session = new SaveSession(conn)) {
            User user = new User();
            user.setName("事务测试");
            session.save(user);
            TransactionManager.commit();
        } catch (Exception e) {
            TransactionManager.rollback();
            throw e;
        }
    }

}
