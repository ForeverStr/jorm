package org.example.test;

import org.example.core.Session;
import org.example.entity.User;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SaveTest {
    //保存测试
    @Test
    void testSaveOperations(){
        try (Session session = new Session()) {
            session.beginTransaction();
            try {
                User user = new User();
                user.setName("test2");
                user.setAge(20);
                session.save(user);
                assertNotNull(user.getId(),"主键自增失败");// 验证自增主键是否生成
                System.out.println("成功保存的id为: " + user.getId());
            }catch (Exception e){
                session.rollback();
                throw new RuntimeException("保存失败",e);
            }
        }
    }

}
