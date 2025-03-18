package org.example.core;

import org.example.base.BaseSession;
import org.example.param.Condition;
import org.example.util.ResultSetMapper;
import org.example.util.SQLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FindSession extends BaseSession<FindSession> {
    private boolean transactionActive = false;
    private final List<Condition> conditions = new ArrayList<>(); // 存储查询条件
    private final List<Object> params = new ArrayList<>();        // 存储参数值
    private static final Logger log = LoggerFactory.getLogger(JormSession.class);
    private String orderBy;
    private Integer limit;

    public FindSession() {
        super(DataSource.getConnection());
    }
    // 链式添加等值条件（如 where("user_name", "admin") → user_name = ?）
    public FindSession where(String column, Object value) {
        conditions.add(new Condition(column, "=", value));
        params.add(value);
        return self();
    }

    // 链式添加带操作符的条件（如 where("age", ">", 20)）
    public FindSession where(String column, String operator, Object value) {
        conditions.add(new Condition(column, operator, value));
        params.add(value);
        return self();
    }

    //链式添加limit限制条件
    public FindSession limit(Integer limit){
        this.limit = limit;
        return self();
    }

    public FindSession orderBy(String orderBy){
        this.orderBy = orderBy;
        return self();
    }
    // 执行查询
    public <T> List<T> find(Class<T> clazz) {
        try {
            String sql = SQLBuilder.buildFindSelect(clazz, conditions,limit,orderBy);
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

    @Override
    protected FindSession self() {
        return this;
    }
}
