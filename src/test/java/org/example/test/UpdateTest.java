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
    @Test
    void testCrudOperations() {
        Closure.transaction(conn ->{
            UpdateSession updateSession = new UpdateSession(conn);
            updateSession.Model(User.class).
                    Where("user_name","批量测试1").
                    Where("age","<",100).
                    Set("user_name","Bob").
                    Set("age", 20).
                    Set("status", "inactive").
                    Update();
        });
    }
}
