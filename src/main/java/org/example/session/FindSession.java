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
        return this;
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
        try {
            String sql = FindBuilder.buildFindSelect(clazz, conditions,limit,orderBy,group,havingConditions,selectClause);
            log.info("查询语句1：{}",sql);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                log.info("查询语句2：{}",stmt);
                ResultSet rs = stmt.executeQuery();
                return ResultSetMapper.mapToList(rs,clazz);
            }
        } catch (SQLException | IllegalAccessException | InstantiationException e) {
            throw new JormException(ErrorCode.QUERY_EXECUTION_FAILED, e);
        }
    }

    @Override
    protected FindSession self() {
        return this;
    }
}
