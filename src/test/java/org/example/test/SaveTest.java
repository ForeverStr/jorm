package org.example.test;

import org.example.core.Session;
import org.example.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SaveTest {

    //保存测试
    @Test
    void testSaveOperations(){
        try (Session session = new Session()) {
            User user = new User();
            user.setName("admin");
            user.setAge(20);
            session.save(user);
            assertNotNull(user.getId());  // 验证自增主键是否生成
            System.out.println("Saved user ID: " + user.getId());
        }
    }

}
