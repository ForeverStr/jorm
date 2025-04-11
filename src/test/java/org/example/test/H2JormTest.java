package org.example.test;

import org.example.entity.User;
import org.example.session.SaveSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.core.io.ClassPathResource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
    @Test
    void testSave() {
        try (SaveSession session = new SaveSession(connection)) {
            User user = new User();
            user.setName("保存测试");
            session.save(user);
            Assertions.assertNotNull(user.getId());
        }
    }

}
