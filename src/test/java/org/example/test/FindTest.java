package org.example.test;

import org.example.core.Session;
import org.example.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FindTest {

    // 查询用户
    @Test
    void testCrudOperations() {
        try (Session session = new Session()) {
            User user = session.find(User.class, 30L);
            assertNotNull(user);
            assertEquals("admin", user.getName());
            System.out.println("User name: " + user.getName());
        }
    }

}
