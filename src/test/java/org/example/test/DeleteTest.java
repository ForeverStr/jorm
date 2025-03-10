package org.example.test;

import org.example.core.Session;
import org.example.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class DeleteTest {

    // 删除用户
    @Test
    void testCrudOperations() {
        try (Session session = new Session()) {
            User user = session.find(User.class, 15L);
            session.delete(user);
        }
    }

}
