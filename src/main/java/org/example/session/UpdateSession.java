package org.example.session;

import org.example.dto.Condition;
import org.example.exception.ErrorCode;
import org.example.exception.JormException;
import org.example.session.base.BaseSession;
import org.example.sqlBuilder.UpdateBuilder;
import org.example.util.EntityHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UpdateSession extends BaseSession<UpdateSession> {
    private List<Condition> conditions = new ArrayList<>();
    private Class<?> entityClass;
    private Object updateEntity;
    private Map<String, Object> updates = new LinkedHashMap<>();
    public UpdateSession() {
        super();
    }
    public UpdateSession(Connection externalConn) {
        super(externalConn);
    }
    public UpdateSession Model(Class<?> entityClass) {
        this.entityClass = entityClass;
        return self();
    }
    public UpdateSession Where(String column, Object value){
        conditions.add(new Condition(column, "=", value));
        return self();
    }
    public UpdateSession Where(String column, String operator, Object value){
        conditions.add(new Condition(column, operator, value));
        return self();
    }
    /**
     * 更新实体（根据条件）
     */
    public void Update(Object entity) {
        this.updateEntity = entity;
        executeEntityUpdate();
    }
    /**
     * 更新指定字段（单个字段）
     */
    public void Update(String column, Object value) {
        updates.put(column, value);
        executeUpdate();
    }

    /**
     * 更新指定字段（多个字段）
     */
    public void Update(Object... columnValuePairs) {
        if (columnValuePairs.length % 2 != 0) {
            throw new JormException(ErrorCode.INVALID_COLUMN_NAME);
        }
        for (int i = 0; i < columnValuePairs.length; i += 2) {
            String column = (String) columnValuePairs[i];
            Object value = columnValuePairs[i + 1];
            updates.put(column, value);
        }
        executeUpdate();
    }
    // 执行实体更新（根据条件）
    private void executeEntityUpdate() {
        validateUpdate();
        Map<String, Object> updateFields = EntityHelper.getNonNullFields(updateEntity);
        if (updateFields.isEmpty()) {
            throw new IllegalStateException("No non-null fields to update");
        }
        executeUpdate(entityClass, conditions, updateFields);
    }
    // 执行指定字段更新（根据条件）
    private void executeUpdate() {
        validateUpdate();
        executeUpdate(entityClass, conditions, updates);
    }
    private void executeUpdate(Class<?> entityClass,
                               List<Condition> conditions,
                               Map<String, Object> updates){
        String sql = UpdateBuilder.buildUpdateSql(entityClass, conditions, updates);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            // 设置SET部分的参数
            for (Object value : updates.values()) {
                stmt.setObject(parameterIndex++, value);
            }
            // 设置WHERE条件的参数
            for (Condition condition : conditions) {
                stmt.setObject(parameterIndex++, condition.getValue());
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update failed", e);
        }finally {
            // 每次执行后重置状态
            resetState();
        }
    }
    private void validateUpdate() {
        if (entityClass == null) throw new JormException(ErrorCode.MODEL_NOT_SPECIFIED);
        if (conditions.isEmpty()) {
            throw new JormException(ErrorCode.CONDITION_NOT_SPECIFIED);
        }
    }
    private void resetState() {
        this.conditions.clear();
        this.updates.clear();
        this.updateEntity = null;
        this.entityClass = null;
    }
    @Override
    protected UpdateSession self() {
        return this;
    }
}
