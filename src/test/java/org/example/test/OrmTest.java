package org.example.test;

import org.example.core.Session;
import org.example.entity.User;  // 确保 User 实体类存在
import org.example.query.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrmTest {

    // 事务管理
    @Test
    void testCrudOperations() {
        try (Session session = new Session()) {
            session.beginTransaction();
            try {
                User user = new User();
                user.setName("Bob");
                session.save(user);
                session.commit();
                assertNotNull(user.getId());
            } catch (Exception e) {
                session.rollback();
                fail("Transaction failed: " + e.getMessage());
            }
        }
    }
}
