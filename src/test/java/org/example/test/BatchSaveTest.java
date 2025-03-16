package org.example.test;

import org.example.core.Session;
import org.example.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class BatchSaveTest {

    // 批量插入
    @Test
    void testCrudOperations() {
        // 正确的批量插入代码
        try (Session session = new Session()) {
            List<User> validUsers = Arrays.asList(
                    new User("Bob", 25,"active"),
                    new User("Charlie", 30,"active")
            );
            
            List<Long> ids = session.batchSave(validUsers);
            System.out.println("插入成功，生成的主键 IDs: " + ids);
        } catch (Exception e) {
            System.err.println("插入失败: " + e.getMessage());
        }
    }

}
