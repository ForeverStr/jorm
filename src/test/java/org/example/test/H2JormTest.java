package org.example.test;

import org.example.entity.User;
import org.example.session.FindSession;
import org.example.session.SaveSession;
import org.example.session.UpdateSession;
import org.example.transaction.Closure;
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
    //保存测试
    @Test
    void testSave() {
        try (SaveSession session = new SaveSession(connection)) {
            User user = new User();
            user.setName("保存测试");
            session.save(user);
            Assertions.assertNotNull(user.getId());
        }
    }
    @Test
    void testCrudOperations() {
        try (SaveSession session = new SaveSession(connection);
             UpdateSession updateSession = new UpdateSession(connection);
             FindSession findSession = new FindSession(connection)) {
            User user1 = new User("更新测试1", 10, "active");
            User user2 = new User("更新测试2", 20, "inactive");
            User user3 = new User("更新测试3", 30, "active");
            session.save(user1);
            session.save(user2);
            session.save(user3);
            List<User> userList1 = findSession.Where("user_name", "更新测试1").Find(User.class);
            Assertions.assertEquals(1, userList1.size());
            updateSession.Model(User.class).
                    Where("user_name","更新测试1").
                    Set("user_name","Bob").
                    Set("age", 20).
                    Set("status", "inactive").
                    Update();
            List<User> userList2 = findSession.Where("user_name", "Bob").Find(User.class);
            Assertions.assertEquals("Bob", userList2.get(0).getName());
            System.out.println(userList2.get(0).getName());
        }
    }

}
