package org.example.session;

import org.example.dto.Condition;
import org.example.exception.ErrorCode;
import org.example.exception.JormException;
import org.example.session.base.BaseSession;
import org.example.sqlBuilder.UpdateBuilder;
import org.example.util.EntityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class UpdateSession extends BaseSession<UpdateSession> {
    private final List<Condition> conditions = new ArrayList<>();
    private Class<?> entityClass;
    private final Map<String, Object> updates = new LinkedHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(UpdateSession.class);
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
    public UpdateSession Set(String column, Object value) {
        updates.put(column, value);
        return self();
    }
    public void Update() {
        executeUpdate();
    }
    private void executeUpdate() {
        if (entityClass == null) throw new JormException(ErrorCode.MODEL_NOT_SPECIFIED);
        if (conditions.isEmpty()) {
            throw new JormException(ErrorCode.CONDITION_NOT_SPECIFIED);
        }
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
            this.conditions.clear();
            this.updates.clear();
            this.entityClass = null;
        }
    }
    @Override
    protected UpdateSession self() {
        return this;
    }
}
