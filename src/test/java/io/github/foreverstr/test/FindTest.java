package io.github.foreverstr.test;

import io.github.foreverstr.session.base.JormSession;
import io.github.foreverstr.transaction.TransactionManager;
import io.github.foreverstr.session.FindSession;
import io.github.foreverstr.entity.User;
import org.junit.jupiter.api.Assertions;
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

    private final Logger log = LoggerFactory.getLogger(FindTest.class);

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
            Assertions.assertEquals(2, userList.size());
            Assertions.assertEquals("研发部",userList.get(0).getDepartment());
        }catch (Exception e){
                TransactionManager.rollback();
            throw new RuntimeException("查询失败",e);
        }finally {
            TransactionManager.release();
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
        }finally {
            TransactionManager.release();
        }
    }
    @Test
    void test() {
        //单表聚合函数查询
        Connection connection = TransactionManager.begin();
        try (JormSession session = new JormSession(connection)) {
            session.saveSession().save(new User("test4", 30, "active"));
            TransactionManager.commit();
            List<User> userList = session.findSession().Where("user_name" ,"test4").Find(User.class);
            TransactionManager.commit();
            assertNotNull(userList);
        }catch (Exception e){
            TransactionManager.rollback();
            throw new RuntimeException("查询失败",e);
        }finally {
            TransactionManager.release();
        }
    }
}
