package org.example.session;

import org.example.session.base.BaseSession;
import org.example.sqlBuilder.UpdateBuilder;
import org.example.util.EntityHelper;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UpdateSession extends BaseSession<UpdateSession> {
    public UpdateSession() {
        super();
    }
    public UpdateSession(Connection externalConn) {
        super(externalConn);
    }
    // 更新实体（根据 ID）
    public <T> void update(T entity) {
        try {
            String sql = UpdateBuilder.buildUpdate(entity.getClass());
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                setUpdateParameters(stmt, entity);
                stmt.executeUpdate();
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("Update failed", e);
        }
    }

    // 批量更新
    public <T> void batchUpdate(List<T> entities) {
        if (entities.isEmpty()) return;

        try {
            String sql = UpdateBuilder.buildUpdate(entities.get(0).getClass());
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

    @Override
    protected UpdateSession self() {
        return this;
    }
}
