package org.example.core;

import org.example.annotation.*;
import org.example.util.EntityHelper;
import org.example.util.SQLBuilder;
import org.example.util.ResultSetMapper;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Session implements AutoCloseable {
    private Connection connection;
    private boolean transactionActive = false;

    // 初始化 Session（使用 HikariCP 连接池）
    public Session() {
        this.connection = DataSource.getConnection();
    }

    // 开启事务
    public void beginTransaction() {
        try {
            connection.setAutoCommit(false);
            transactionActive = true;
        } catch (SQLException e) {
            throw new RuntimeException("Begin transaction failed", e);
        }
    }

    // 提交事务
    public void commit() {
        try {
            connection.commit();
            transactionActive = false;
        } catch (SQLException e) {
            throw new RuntimeException("Commit failed", e);
        }
    }

    // 回滚事务
    public void rollback() {
        try {
            connection.rollback();
            transactionActive = false;
        } catch (SQLException e) {
            throw new RuntimeException("Rollback failed", e);
        }
    }

    // 保存实体（INSERT）
    public <T> void save(T entity) {
        try {
            String sql = SQLBuilder.buildInsert(entity.getClass());
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setInsertParameters(stmt, entity);
                stmt.executeUpdate();
                // 处理自增主键
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    setIdValue(entity, rs.getLong(1));
                }
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("Save failed", e);
        }
    }

    // 根据 ID 查询（SELECT）
    public <T> T find(Class<T> clazz, Object id) {
        try {
            String sql = SQLBuilder.buildSelectById(clazz);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setObject(1, id);
                ResultSet rs = stmt.executeQuery();
                return ResultSetMapper.mapToEntity(rs, clazz);
            }
        } catch (SQLException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Find failed", e);
        }
    }

    // 关闭连接
    @Override
    public void close() {
        if (connection != null) {
            try {
                if (transactionActive) rollback();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 工具方法：设置 INSERT 参数
    private <T> void setInsertParameters(PreparedStatement stmt, T entity) throws SQLException, IllegalAccessException {
        Class<?> clazz = entity.getClass();
        List<Field> fields = EntityHelper.getInsertableFields(clazz);
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            field.setAccessible(true);
            stmt.setObject(i + 1, field.get(entity));
        }
    }

    // 工具方法：设置自增主键值
    private <T> void setIdValue(T entity, Object idValue) throws IllegalAccessException {
        Field idField = EntityHelper.getIdField(entity.getClass());
        if (idField != null) {
            idField.setAccessible(true);
            // 如果是 Long 类型，直接赋值；如果是其他类型，转换后再赋值
            if (idField.getType() == Long.class) {
                idField.set(entity, idValue);
            } else {
                idField.set(entity, ((Number) idValue).longValue());
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public boolean isTransactionActive() {
        return transactionActive;
    }

    public void setTransactionActive(boolean transactionActive) {
        this.transactionActive = transactionActive;
    }
}
