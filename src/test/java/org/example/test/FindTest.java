package org.example.test;

import org.example.core.Session;
import org.example.entity.User;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FindTest {

    private static final Logger log = LoggerFactory.getLogger(SaveTest.class);

    // 查询用户
    @Test
    void testCrudOperations() {
        try (Session session = new Session()) {
            session.beginTransaction();
            try {
                User user = session.find(User.class, 30L);
                assertNotNull(user);
                log.debug("{}",user);
                assertEquals("admin", user.getName());
                System.out.println("User name: " + user.getName());
            }catch (Exception e){
                session.rollback();
                throw new RuntimeException("查询失败",e);
            }
        }
    }

}
