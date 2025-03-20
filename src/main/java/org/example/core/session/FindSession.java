package org.example.core.session;

import org.example.base.BaseSession;
import org.example.core.DataSource;
import org.example.core.JormSession;
import org.example.param.Condition;
import org.example.util.ResultSetMapper;
import org.example.util.SQLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FindSession extends BaseSession<FindSession> {
    private final List<Condition> conditions = new ArrayList<>(); // 存储查询条件
    private final List<Object> params = new ArrayList<>();        // 存储参数值
    private String group;
    private String having;
    private String selectClause = "*"; // 默认 SELECT *
    private String orderBy;
    private Integer limit;
    private static final Logger log = LoggerFactory.getLogger(JormSession.class);

    public FindSession() {
        super(DataSource.getConnection());
    }

    // 设置 SELECT 子句（支持聚合函数）
    public FindSession Select(String selectClause) {
        this.selectClause = selectClause;
        return this;
    }
    //链式添加having条件
    public FindSession Having(String having){
        this.having = having;
        return self();
    }
    //链式添加group条件
    public FindSession Group(String group){
        this.group = group;
        return self();
    }
    // 链式添加等值条件（如 where("user_name", "admin") → user_name = ?）
    public FindSession Where(String column, Object value) {
        conditions.add(new Condition(column, "=", value));
        params.add(value);
        return self();
    }

    // 链式添加带操作符的条件（如 where("age", ">", 20)）
    public FindSession Where(String column, String operator, Object value) {
        conditions.add(new Condition(column, operator, value));
        params.add(value);
        return self();
    }

    //链式添加limit限制条件
    public FindSession Limit(Integer limit){
        this.limit = limit;
        return self();
    }

    public FindSession Order(String orderBy){
        this.orderBy = orderBy;
        return self();
    }
    // 执行查询
    public <T> List<T> Find(Class<T> clazz) {
        try {
            String sql = SQLBuilder.buildFindSelect(clazz, conditions,limit,orderBy,group,having,selectClause);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                // 绑定参数
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                ResultSet rs = stmt.executeQuery();
                return ResultSetMapper.mapToList(rs,clazz);
            }
        } catch (SQLException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Query failed", e);
        }
    }

    @Override
    protected FindSession self() {
        return this;
    }
}
