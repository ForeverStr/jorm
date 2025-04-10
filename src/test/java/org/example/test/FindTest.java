package org.example.test;

import org.example.transaction.TransactionManager;
import org.example.exception.ErrorCode;
import org.example.exception.JormException;
import org.example.session.FindSession;
import org.example.entity.User;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 单表查询操作
 * @author 杜玉杰
 */
public class FindTest {

    private final Logger log = LoggerFactory.getLogger(SaveTest.class);

    @Test
    void testCrudOperations() {
        //单表基础查询
        Connection connection = TransactionManager.begin();
        try (FindSession findSession = new FindSession(connection)) {
            List<User> userList = findSession
                    .Where("user_name","Alice")
                    .Where("age","<",50)
                    .Order("age")
                    .Limit(2)
                    .Find(User.class);
            TransactionManager.commit();
            assertNotNull(userList);
            System.out.println(userList.get(0).getAge());
            System.out.println(userList.get(1).getAge());
        }catch (Exception e){
                TransactionManager.rollback();
                throw new JormException(ErrorCode.QUERY_FAILED,e);
        }
    }
    @Test
    void testFindByAggregation() {
        //单表聚合函数查询
        Connection connection = TransactionManager.begin();
        try (FindSession findSession = new FindSession(connection)) {
            List<User> userList = findSession
                    .Select("department, SUM(age) AS totalAge")
                    .Where("status","active")
                    .Group("department")
                    .Having("totalAge", ">", 200)
                    .Find(User.class);
            TransactionManager.commit();
            assertNotNull(userList);
            for (User user : userList){
                System.out.println(user.getDepartment());
                System.out.println(user.getTotalAge());
            }
        }catch (Exception e){
            TransactionManager.rollback();
            throw new RuntimeException("查询失败",e);
        }
    }
}
