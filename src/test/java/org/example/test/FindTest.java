package org.example.test;

import org.example.core.session.FindSession;
import org.example.entity.User;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FindTest {

    private static final Logger log = LoggerFactory.getLogger(SaveTest.class);

    // 单表查询操作
    @Test
    void testCrudOperations() {
        //单表基础查询
        try (FindSession findSession = new FindSession()) {
            findSession.beginTransaction();
            try {
                List<User> userList = findSession
                        .Where("user_name","Alice")
                        .Where("age","<",50)
                        .Order("age")
                        .Limit(2)
                        .Find(User.class);
                findSession.commit();
                assertNotNull(userList);
                System.out.println(userList.get(0).getAge());
                System.out.println(userList.get(1).getAge());
            }catch (Exception e){
                findSession.rollback();
                throw new RuntimeException("查询失败",e);
            }
        }
        //单表聚合函数查询
        try (FindSession findSession = new FindSession()) {
            findSession.beginTransaction();
            try {
                List<User> userList = findSession
                        .Select("department,SUM(age) as totalAge")
                        .Where("status","active")
                        .Group("department")
                        .Having("totalAge > 200")
                        .Find(User.class);
                findSession.commit();
                assertNotNull(userList);
                for (User user : userList){
                    System.out.println(user.getDepartment());
                    System.out.println(user.getTotalAge());
                }
            }catch (Exception e){
                findSession.rollback();
                throw new RuntimeException("查询失败",e);
            }
        }
    }

}
