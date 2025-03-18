package org.example.test;

import org.example.core.FindSession;
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
        try (FindSession findSession = new FindSession()) {
            findSession.beginTransaction();
            try {
                List<User> userList = findSession
                        .where("user_name","Alice")
                        .where("age","<",50)
                        .orderBy("age")
                        .limit(2)
                        .find(User.class);
                findSession.commit();
                assertNotNull(userList);
                System.out.println(userList.get(0).getAge());
                System.out.println(userList.get(1).getAge());
            }catch (Exception e){
                findSession.rollback();
                throw new RuntimeException("查询失败",e);
            }
        }
    }

}
