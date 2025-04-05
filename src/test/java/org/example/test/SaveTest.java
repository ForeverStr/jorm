package org.example.test;

import org.example.entity.User;
import org.example.session.SaveSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;



public class SaveTest {
    //保存测试
    @Test
    void testSave() {
        try (SaveSession session = new SaveSession()) {
            User user = new User();
            user.setName("保存测试");
            session.save(user);
            Assertions.assertNotNull(user.getId());
        }
    }

}
