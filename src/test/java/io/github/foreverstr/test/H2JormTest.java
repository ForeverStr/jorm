package io.github.foreverstr.test;

import io.github.foreverstr.entity.User;
import io.github.foreverstr.session.DeleteSession;
import io.github.foreverstr.session.FindSession;
import io.github.foreverstr.session.SaveSession;
import io.github.foreverstr.session.UpdateSession;
import io.github.foreverstr.session.base.JormSession;
import io.github.foreverstr.session.factory.Jorm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.core.io.ClassPathResource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
public class H2JormTest {
    private Connection connection;
    private String dbName;
    @BeforeEach
    void setUp() throws SQLException {
        // 生成唯一的数据库名
        dbName = "test_" + UUID.randomUUID().toString().replace("-", "");
        // 构建 JDBC URL
        String jdbcUrl = String.format(
                "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false",
                dbName
        );
        // 获取数据库连接
        connection = DriverManager.getConnection(jdbcUrl, "sa", "");
        // 执行 SQL 脚本初始化表结构
        ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
    }
    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close(); // 关闭连接后，内存数据库自动销毁
        }
    }
    //工厂模式测试
    @Test
    void testFactoryCRUD() {
        try (SaveSession session = Jorm.saveSession(connection)) {
            User user = new User("保存测试", 20, "active");
            session.save(user);
        }
        try (UpdateSession session = Jorm.updateSession(connection)) {
            session.Model(User.class).
                    Where("user_name", "保存测试").
                    Set("user_name", "更新测试").
                    Update();
        }
        try (FindSession session = Jorm.findSession(connection)) {
            List<User> userList = session.Where("user_name", "更新测试").Find(User.class);
            Assertions.assertEquals(1, userList.size());
            Assertions.assertEquals("更新测试", userList.get(0).getName());
            Assertions.assertEquals(20, userList.get(0).getAge());
        }
        try (DeleteSession session = Jorm.deleteSession(connection)) {
            session.Where("user_name", "更新测试").Delete(User.class);
        }
        try (FindSession session = Jorm.findSession(connection)) {
            List<User> userList = session.Where("user_name", "更新测试").Find(User.class);
            Assertions.assertEquals(0, userList.size());
        }
    }
    //统一接口模式测试
    @Test
    void testInterfaceCRUD() {
        try (JormSession session = new JormSession(connection)) {
            User user1 = new User("更新测试1", 10, "active");
            User user2 = new User("更新测试2", 10, "inactive");
            User user3 = new User("更新测试3", 10, "active");
            session.saveSession().save(user1);
            session.saveSession().save(user2);
            session.saveSession().save(user3);

            session.updateSession().Model(User.class).
                    Where("user_name","更新测试1").
                    Set("user_name","Bob").
                    Set("status", "inactive").
                    Update();
            List<User> userList1 = session.findSession().Where("user_name", "Bob").Find(User.class);
            Assertions.assertEquals("Bob", userList1.get(0).getName());
            Assertions.assertEquals(10, userList1.get(0).getAge());

            List<User> userList2 = session.findSession().Where("age", 10).Find(User.class);
            Assertions.assertEquals(3, userList2.size());
            Assertions.assertEquals("Bob", userList2.get(0).getName());
            Assertions.assertEquals("更新测试2", userList2.get(1).getName());

            session.deleteSession().Where("user_name","Bob").Delete(User.class);

            List<User> userList3 = session.findSession().Where("age", 10).Find(User.class);
            Assertions.assertEquals(2, userList3.size());
            Assertions.assertEquals("更新测试2", userList3.get(0).getName());
        }
    }
}
