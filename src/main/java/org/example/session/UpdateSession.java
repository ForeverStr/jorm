package org.example.session;

import org.example.dto.Condition;
import org.example.exception.ErrorCode;
import org.example.exception.JormException;
import org.example.session.base.BaseSession;
import org.example.sqlBuilder.UpdateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p> 继承自BaseSession，实现了对数据库的更新操作 </p>
 * <p> 用于更新指定实体类的指定字段，并根据条件进行更新 </p>
 * <p> 调用Update()方法即可执行更新 </p>
 * <p> 示例： </p>
 * <pre>
 *     try(UpdateSession session = new UpdateSession()) {
 *           session.Model(User.class)
 *                  .Where("id", 1)
 *                  .Set("name", "Tom")
 *                  .Update();
 *     }
 * </pre>
 * @author duyujie
 * @version 1.0
 * @see BaseSession
 * @see UpdateBuilder
 */
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
    /**
     * 指定更新目标实体类
     * @param entityClass 实体类
     * @return 当前对象 以便链式调用
     */
    public UpdateSession Model(Class<?> entityClass) {
        this.entityClass = entityClass;
        return self();
    }
    /**
     * 指定更新条件
     * @param column 列名
     * @param value 值
     * @return 当前对象 以便链式调用
     */
    public UpdateSession Where(String column, Object value){
        conditions.add(new Condition(column, "=", value));
        return self();
    }
    /**
     * 指定更新条件
     * @param column 列名
     * @param operator 操作符
     * @param value 值
     * @return 当前对象 以便链式调用
     */
    public UpdateSession Where(String column, String operator, Object value){
        conditions.add(new Condition(column, operator, value));
        return self();
    }
    /**
     * 指定更新字段
     * @param column 列名
     * @param value 值
     * @return 当前对象 以便链式调用
     */
    public UpdateSession Set(String column, Object value) {
        updates.put(column, value);
        return self();
    }
    /**
     * 执行更新操作
     * @throws JormException 更新异常
     */
    public void Update() {
        checkIfClosed();

        if (entityClass == null) {
            String errorMsg = "未指定更新目标实体类";
            log.error("[ErrorCode={}] {}", ErrorCode.MODEL_NOT_SPECIFIED.getCode(), errorMsg);
            throw new JormException(ErrorCode.MODEL_NOT_SPECIFIED, errorMsg);
        }
        if (updates == null || updates.isEmpty()) {
            String errorMsg = "更新字段不能为空";
            log.error("[ErrorCode={}] {}", ErrorCode.UPDATE_FIELD_EMPTY.getCode(), errorMsg);
            throw new JormException(ErrorCode.UPDATE_FIELD_EMPTY, errorMsg);
        }
        if (conditions.isEmpty()) {
            String errorMsg = "未指定更新条件";
            log.error("[ErrorCode={}] {}", ErrorCode.CONDITION_NOT_SPECIFIED.getCode(), errorMsg);
            throw new JormException(ErrorCode.CONDITION_NOT_SPECIFIED, errorMsg);
        }

        String sql = null;
        try {
            sql = UpdateBuilder.buildUpdateSql(entityClass, conditions, updates);
            log.debug("生成更新SQL: [{}], SET参数: {}, WHERE参数: {}",
                    sql,
                    updates.values(),
                    conditions.stream().map(Condition::getValue).collect(Collectors.toList())
            );

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                int parameterIndex = 1;
                // 绑定SET参数
                for (Object value : updates.values()) {
                    if (value == null) {
                        log.warn("更新字段值为null，可能引发潜在问题");
                    }
                    stmt.setObject(parameterIndex++, value);
                }
                // 绑定WHERE参数
                for (Condition condition : conditions) {
                    Object conditionValue = condition.getValue();
                    if (conditionValue == null) {
                        log.warn("条件值为null");
                    }
                    stmt.setObject(parameterIndex++, conditionValue);
                }
                // 执行并记录影响行数
                int affectedRows = stmt.executeUpdate();
                log.debug("更新成功: [影响行数={}]", affectedRows);

                if (affectedRows == 0) {
                    log.warn("更新操作未影响任何行，请检查条件有效性");
                }
            }
        } catch (SQLException e) {
            String errorMsg = String.format("SQL执行失败 [SQL=%s]", sql);
            log.error("[ErrorCode={}] {}", ErrorCode.UPDATE_EXECUTION_FAILED.getCode(), errorMsg, e);
            throw new JormException(ErrorCode.UPDATE_EXECUTION_FAILED, errorMsg, e);
        } finally {
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
