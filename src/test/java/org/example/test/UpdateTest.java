package org.example.test;

import org.example.entity.User;
import org.example.session.FindSession;
import org.example.session.SaveSession;
import org.example.session.UpdateSession;
import org.example.transaction.Closure;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;


public class UpdateTest {

    // 更新用户
//    @Test
//    void testCrudOperations() {
//        Closure.transaction(conn ->{
//            SaveSession saveSession = new SaveSession(conn);
//            User user1 = new User("更新测试1", 22, "active");
//            User user2 = new User("更新测试2", 23, "inactive");
//            saveSession.save(user1);
//            saveSession.save(user2);
//
//            UpdateSession updateSession = new UpdateSession(conn);
//            updateSession.Where("id",user1.getId()).Update("age",33);
//            updateSession.Where("id",user2.getId()).Update("status","active");
//
//            FindSession findSession = new FindSession(conn);
//            List<User> users = findSession.Where("age",23).Find(User.class);
//            for (User user : users) {
//                System.out.println(user.getName() + " " + user.getAge() + " " + user.getStatus());
//            }
//        });
//    }
}
