package org.example.core;

import org.example.annotation.*;
import org.example.util.EntityHelper;
import org.example.util.SQLBuilder;
import org.example.util.SessionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class JormSession implements AutoCloseable {
    private Connection connection;
    private boolean transactionActive = false;
    private static final Logger log = LoggerFactory.getLogger(JormSession.class);
    // 初始化 Session（使用 HikariCP 连接池）
    public JormSession() {
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
            log.info("预编译前的sql：{}",sql);
            //PreparedStatement预编译sql语句，提高性能，后续的set方法明确sql语句的结构和参数值，避免sql注入，还可以自动获取主键值
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
            {
                log.info("预编译后的sql：{}",stmt);
                SessionHelper.setInsertParameters(stmt, entity);
                stmt.executeUpdate();
                // 处理自增主键,ResultSet数据库查询结果集
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    SessionHelper.setIdValue(entity, rs.getLong(1));
                }
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("Save failed", e);
        }
    }

    // 更新实体（根据 ID）
    public <T> void update(T entity) {
        try {
            String sql = SQLBuilder.buildUpdate(entity.getClass());
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                setUpdateParameters(stmt, entity);
                stmt.executeUpdate();
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("Update failed", e);
        }
    }

    // 删除实体（根据 ID）
    public <T> void delete(T entity) {
        try {
            String sql = SQLBuilder.buildDelete(entity.getClass());
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                Object idValue = EntityHelper.getIdValue(entity);
                stmt.setObject(1, idValue);
                stmt.executeUpdate();
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("删除失败", e);
        }
    }

    // 批量插入（返回生成的主键列表）
    public <T> List<Long> batchSave(List<T> entities) {
        log.debug("批量插入 SQL: {}", SQLBuilder.buildInsert(entities.get(0).getClass()));
        //非空逻辑校验
        for (T entity : entities) {
            validateEntity(entity);
        }
        if (entities.isEmpty()) return Collections.emptyList();

        try {
            String sql = SQLBuilder.buildInsert(entities.get(0).getClass());
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (T entity : entities) {
                    SessionHelper.setInsertParameters(stmt, entity);
                    stmt.addBatch();
                }
                stmt.executeBatch();

                // 获取所有生成的主键
                ResultSet rs = stmt.getGeneratedKeys();
                List<Long> ids = new ArrayList<>();
                while (rs.next()) {
                    ids.add(rs.getLong(1));
                }
                return ids;
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("Batch save failed", e);
        }
    }

    // 批量更新
    public <T> void batchUpdate(List<T> entities) {
        if (entities.isEmpty()) return;

        try {
            String sql = SQLBuilder.buildUpdate(entities.get(0).getClass());
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (T entity : entities) {
                    setUpdateParameters(stmt, entity);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("Batch update failed", e);
        }
    }

    private <T> void validateEntity(T entity) {
        // 遍历所有字段，检查 @Column(nullable = false) 的字段是否非空
        for (Field field : entity.getClass().getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null && !column.nullable()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    if (value == null) {
                        throw new IllegalArgumentException(
                                "Field '" + field.getName() + "' cannot be null"
                        );
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Validation failed", e);
                }
            }
        }
    }

    // 工具方法：设置 UPDATE 参数（排除主键）
    private <T> void setUpdateParameters(PreparedStatement stmt, T entity)
            throws SQLException, IllegalAccessException {
        Class<?> clazz = entity.getClass();
        List<Field> fields = EntityHelper.getUpdatableFields(clazz);
        Field idField = EntityHelper.getIdField(clazz);

        // 设置非主键字段
        int index = 1;
        for (Field field : fields) {
            field.setAccessible(true);
            stmt.setObject(index++, field.get(entity));
        }

        // 设置 WHERE 主键条件
        idField.setAccessible(true);
        stmt.setObject(index, idField.get(entity));
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
