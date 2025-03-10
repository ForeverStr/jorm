package org.example.test;

import org.example.core.Session;
import org.example.entity.User;
import org.example.query.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class QueryTest {

    // 链式查询
    @Test
    void testCrudOperations() {
        try (Session session = new Session()) {
            List<User> users = new Query<>(User.class, session)
                    .where("age > :age").param("age", 18).orderBy("username DESC")//name改为username,数据库字段而非实体类字段
                    .limit(10)
                    .list();
            assertFalse(users.isEmpty());
            users.forEach(u -> System.out.println(u.getName()));
        }
    }

}
