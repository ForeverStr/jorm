package org.example.session;

import org.example.exception.ErrorCode;
import org.example.session.base.BaseSession;
import org.example.exception.JormException;
import org.example.dto.Condition;
import org.example.sqlBuilder.FindBuilder;
import org.example.util.ResultSetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 用于查询的会话
 * </p>
 */
public class FindSession extends BaseSession<FindSession> {
    private final List<Condition> conditions = new ArrayList<>();
    private final List<Condition> havingConditions = new ArrayList<>();
    private final List<Object> params = new ArrayList<>();
    private String group;
    private String selectClause = "*";
    private String orderBy;
    private Integer limit;
    private static final Logger log = LoggerFactory.getLogger(FindSession.class);

    public FindSession() {
        super();
    }
    public FindSession(Connection externalConn) {
        super(externalConn);
    }
    /**
     * 设置 SELECT 子句（支持聚合函数）
     */
    public FindSession Select(String selectClause) {
        this.selectClause = selectClause;
        return self();
    }
    /**
     * 链式添加having条件
     */
    public FindSession Having(String column, String operator, Object value) {
        havingConditions.add(new Condition(column, operator, value));
        params.add(value);
        return self();
    }
    /**
     * 链式添加group条件
     */
    public FindSession Group(String group){
        this.group = group;
        return self();
    }
    /**
     * 链式添加等值条件
     */
    public FindSession Where(String column, Object value) {
        conditions.add(new Condition(column, "=", value));
        params.add(value);
        return self();
    }
    /**
     * 链式添加带操作符的条件
     */
    public FindSession Where(String column, String operator, Object value) {
        conditions.add(new Condition(column, operator, value));
        params.add(value);
        return self();
    }
    /**
     * 链式添加limit限制条件
     */
    public FindSession Limit(Integer limit){
        this.limit = limit;
        return self();
    }
    /**
     * 链式添加order by条件
     */
    public FindSession Order(String orderBy){
        this.orderBy = orderBy;
        return self();
    }
    /**
     * 执行查询
     */
    public <T> List<T> Find(Class<T> clazz) {
        // 前置校验：确保连接和参数合法
        checkIfClosed();
        if (clazz == null) {
            log.error("[ErrorCode={}] 模型未指定", ErrorCode.MODEL_NOT_SPECIFIED.getCode());
            throw new JormException(ErrorCode.MODEL_NOT_SPECIFIED, "模型未指定");
        }

        String sql = null;
        try {
            sql = FindBuilder.buildFindSelect(clazz, conditions, limit, orderBy, group, havingConditions, selectClause);
            log.debug("Generated SQL: [{}], Parameters: {}", sql, params);

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                log.debug("Executing query with parameters: {}", params);
                ResultSet rs = stmt.executeQuery();
                return ResultSetMapper.mapToList(rs, clazz);
            }
        } catch (SQLException e) {
            String errorMsg = String.format("SQL执行失败 [SQL=%s, Params=%s]", sql, params);
            log.error("[ErrorCode={}] {}", ErrorCode.QUERY_EXECUTION_FAILED.getCode(), errorMsg, e);
            throw new JormException(ErrorCode.QUERY_EXECUTION_FAILED, errorMsg, e);
        } catch (IllegalAccessException | InstantiationException e) {
            String errorMsg = String.format("结果映射失败 [Class=%s]", clazz.getName());
            log.error("[ErrorCode={}] {}", ErrorCode.RESULT_MAPPING_FAILED.getCode(), errorMsg, e);
            throw new JormException(ErrorCode.RESULT_MAPPING_FAILED, errorMsg, e);
        } catch (Exception e) {
            String errorMsg = String.format("未知查询错误 [SQL=%s]", sql);
            log.error("[ErrorCode={}] {}", ErrorCode.UNKNOWN_QUERY_ERROR.getCode(), errorMsg, e);
            throw new JormException(ErrorCode.UNKNOWN_QUERY_ERROR, errorMsg, e);
        } finally {
            resetState(); // 确保每次执行后状态重置
        }
    }
    private void resetState() {
        this.conditions.clear();
        this.havingConditions.clear();
        this.params.clear();
        this.group = null;
        this.selectClause = "*";
        this.orderBy = null;
        this.limit = null;
    }

    @Override
    protected FindSession self() {
        return this;
    }
}
