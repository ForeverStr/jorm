package org.example.test;

import org.example.transaction.Closure;
import org.example.entity.User;
import org.example.session.DeleteSession;
import org.example.session.FindSession;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class DeleteTest {

    // 删除对象(单个实例对象)
    @Test
    void testCrudOperations() {
        Closure.transaction(conn -> {
            FindSession findSession = new FindSession(conn);
            List<User> userList  = findSession.Where("age",100).Find(User.class);
            DeleteSession deleteSession = new DeleteSession(conn);
            deleteSession.Delete(userList.get(0));
        });
    }
    // 删除对象(集合对象)
    @Test
    void testDeleteCollection() {
        Closure.transaction(conn -> {
            FindSession findSession = new FindSession(conn);
            List<User> userList  = findSession.Where("age",100).Find(User.class);
            DeleteSession deleteSession = new DeleteSession(conn);
            deleteSession.Delete(userList);
        });
    }
    // 删除对象(类对象)
    @Test
    void testDelete() {
        try(DeleteSession deleteSession = new DeleteSession()){
            deleteSession.Where("age",1).Limit(2).Delete(User.class);
        }
    }

}
