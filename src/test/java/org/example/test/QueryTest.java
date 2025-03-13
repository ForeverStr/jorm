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
            session.beginTransaction();
            try {
                List<User> users = new Query<>(User.class, session)
                        .where("age > :age").param("age", 18).orderBy("username DESC")
                        .limit(10)
                        .list();
                assertFalse(users.isEmpty());
                users.forEach(u -> System.out.println(u.getName()));
            }catch (Exception e){
                session.rollback();
                throw new RuntimeException("查询失败",e);
            }
        }
    }

}
