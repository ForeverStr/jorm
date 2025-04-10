package org.example.session;

import org.example.base.BaseSession;
import org.example.dto.Condition;
import org.example.exception.ErrorCode;
import org.example.exception.JormException;
import org.example.sqlBuilder.DeleteBuilder;
import org.example.util.EntityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DeleteSession extends BaseSession<DeleteSession> {
    private final List<Condition> conditions = new ArrayList<>();
    private final List<Object> params = new ArrayList<>();
    private int limit;
    private static final Logger log = LoggerFactory.getLogger(FindSession.class);
    public DeleteSession() {
        super();
    }
    public DeleteSession(Connection externalConn) {
        super(externalConn);
    }
    public DeleteSession Where(String column, Object value) {
        conditions.add(new Condition(column, "=", value));
        params.add(value);
        return self();
    }
    public DeleteSession Limit(int limit) {
        this.limit = limit;
        return this;
    }
    // 执行删除（实例对象）
    public <T> void Delete(T entity) {
        if (entity instanceof Collection) {
            deleteBatch((Collection<?>) entity);
        } else {
            deleteSingle(entity);
        }
    }
    // 执行单个实例对象删除
    private <T> void deleteSingle(T entity) {
        try {
            String sql = DeleteBuilder.buildSingleDelete(entity.getClass());
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                Object idValue = EntityHelper.getIdValue(entity);
                stmt.setObject(1, idValue);
                stmt.executeUpdate();
                log.info("删除单个对象语句：{}",stmt);
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new JormException(ErrorCode.DELETE_ERROR, e);
        }
    }
    // 执行批量实例对象删除
    private <T> void deleteBatch(Collection<T> entities) {
        if (entities.isEmpty()) return;
        try {
            List<T> entityList = new ArrayList<>(entities);
            String sql = DeleteBuilder.buildBatchDelete(entityList.get(0).getClass(), new ArrayList<>(entities));
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < entityList.size(); i++) {
                    T entity = entityList.get(i);
                    stmt.setObject(i + 1, EntityHelper.getIdValue(entity));
                }
                stmt.executeUpdate();
                log.info("删除类对象语句：{}",stmt);
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new JormException(ErrorCode.DELETE_ERROR, e);
        }
    }

    // 执行删除（类对象）
    public <T> void Delete(Class<T> clazz) {
        try {
            String sql = DeleteBuilder.buildClassDelete(clazz,conditions,limit);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    for (int i = 0; i < params.size(); i++) {
                        stmt.setObject(i + 1, params.get(i));
                    }
                stmt.executeUpdate();
                log.info("删除集合对象语句：{}",stmt);
            }
        } catch (SQLException e) {
            throw new JormException(ErrorCode.DELETE_ERROR, e);
        }
    }
    @Override
    protected DeleteSession self() {
        return this;
    }
}
