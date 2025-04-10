package org.example.test;

import org.example.entity.User;
import org.example.session.SaveSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;


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
    //批量插入测试
    @Test
    void testBatchInsert() {
        // 正确的批量插入代码
        try (SaveSession session = new SaveSession()) {
            List<User> validUsers = Arrays.asList(
                    new User("批量测试1", 25,"active"),
                    new User("批量测试2", 30,"active")
            );

            List<Long> ids = session.batchSave(validUsers);
            System.out.println("插入成功，生成的主键 IDs: " + ids);
        } catch (Exception e) {
            System.err.println("插入失败: " + e.getMessage());
        }
    }

}
