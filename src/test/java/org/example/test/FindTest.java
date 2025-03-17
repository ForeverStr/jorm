package org.example.test;

import org.example.core.JormSession;
import org.example.entity.User;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FindTest {

    private static final Logger log = LoggerFactory.getLogger(SaveTest.class);

    // 查询用户
    @Test
    void testCrudOperations() {
        try (JormSession session = new JormSession()) {
            session.beginTransaction();
            try {
                List<User> userList = session
                        .where("user_name","Alice")
                        .where("age","=",25)
                        .find(User.class);
                assertNotNull(userList);
                System.out.println("size: " + userList.size());
            }catch (Exception e){
                session.rollback();
                throw new RuntimeException("查询失败",e);
            }
        }
    }

}
