package io.github.foreverstr.test;

import io.github.foreverstr.session.base.JormSession;
import io.github.foreverstr.session.SaveSession;
import io.github.foreverstr.session.UpdateSession;
import io.github.foreverstr.transaction.Closure;
import io.github.foreverstr.entity.User;
import io.github.foreverstr.session.DeleteSession;
import io.github.foreverstr.session.FindSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class DeleteTest {

    // 删除对象(实例对象)
    @Test
    void testCrudOperations() {
        Closure.transaction(conn -> {
            SaveSession saveSession = new SaveSession(conn);
            User user1 = new User("DeleteUser1", 30, "active");
            User user2 = new User("DeleteUser1", 30, "active");
            User user3 = new User("DeleteUser1", 30, "active");
            saveSession.save(user1);
            saveSession.save(user2);
            saveSession.save(user3);
            FindSession findSession = new FindSession(conn);
            List<User> userList1  = findSession.Where("user_name","DeleteUser1").Find(User.class);
            Assertions.assertEquals(3, userList1.size());

            UpdateSession updateSession = new UpdateSession(conn);
            updateSession.Model(User.class).Where("user_name", "DeleteUser1")
                           .Set("status", "inactive")
                           .Update();
            List<User> userList2  = findSession.Where("user_name","DeleteUser1").Find(User.class);
            Assertions.assertTrue(userList2.stream().allMatch(u -> u.getStatus().equals("inactive")));

            DeleteSession deleteSession = new DeleteSession(conn);
            deleteSession.Delete(userList2.get(0));
            deleteSession.Delete(userList2);

            List<User> userList3  = findSession.Where("user_name","DeleteUser1").Find(User.class);
            Assertions.assertTrue(userList3.isEmpty());
        });
    }
    // 删除对象(类对象)
    @Test
    void testDelete() {
        try(JormSession session = new JormSession()){
            session.saveSession().save(new User("DeleteUser2", 30, "active"));
            session.saveSession().save(new User("DeleteUser2", 30, "active"));
            List<User> userList1 = session.findSession().Where("user_name","DeleteUser2").Find(User.class);
            Assertions.assertEquals(2, userList1.size());
            session.deleteSession().Where("user_name","DeleteUser2").Limit(2).Delete(User.class);
            List<User> userList2 = session.findSession().Where("user_name","DeleteUser2").Find(User.class);
            Assertions.assertEquals(0, userList2.size());
        }
    }

}
