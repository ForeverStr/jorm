package org.example.test;

import org.example.core.Session;
import org.example.entity.User;  // 确保 User 实体类存在
import org.example.query.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrmTest {

    @Test
    void testCrudOperations() {
        // 示例 1: 保存用户
        try (Session session = new Session()) {
            User user = new User();
            user.setName("Alice");
            user.setAge(25);
            session.save(user);
            assertNotNull(user.getId());  // 验证自增主键是否生成
            System.out.println("Saved user ID: " + user.getId());
        }

        // 示例 2: 查询用户
        try (Session session = new Session()) {
            User user = session.find(User.class, 15L);  // 假设 ID=1 存在
            assertNotNull(user);
            assertEquals("Alice", user.getName());
            System.out.println("User name: " + user.getName());
        }

        // 示例 3: 链式查询
        try (Session session = new Session()) {
            List<User> users = new Query<>(User.class, session)
                    .where("age > :age")
                    .param("age", 18)
                    .orderBy("username DESC")//name改为username,数据库字段而非实体类字段
                    .limit(10)
                    .list();
            assertFalse(users.isEmpty());
            users.forEach(u -> System.out.println(u.getName()));
        }

        // 示例 4: 事务管理
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
