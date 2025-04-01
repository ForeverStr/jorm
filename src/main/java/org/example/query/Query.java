package org.example.query;

import org.example.core.JormSession;
import org.example.util.ResultSetMapper;
import org.example.core.SQLBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Query<T> {
    private final Class<T> clazz;
    private final JormSession session;
    private final List<String> conditions = new ArrayList<>();
    private final Map<String, Object> params = new HashMap<>();
    private final List<Object> orderedParams = new ArrayList<>();
    private String orderBy;
    private Integer limit;

    public Query(Class<T> clazz, JormSession session) {
        this.clazz = clazz;
        this.session = session;
    }

    public Query<T> where(String condition) {
        conditions.add(condition);
        return this;
    }

    // 记录参数值到 orderedParams
    public Query<T> param(String name, Object value) {
        params.put(name, value);
        orderedParams.add(value); // 按添加顺序存储值
        return this;
    }

    public Query<T> orderBy(String field) {
        this.orderBy = field;
        return this;
    }

    public Query<T> limit(int limit) {
        this.limit = limit;
        return this;
    }

    public List<T> list() {
        try {
            List<String> paramNames = new ArrayList<>();
            String sql = SQLBuilder.buildSelect(clazz, conditions, orderBy, limit, paramNames);
            try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
                // 按 orderedParams 顺序设置参数
                for (int i = 0; i < orderedParams.size(); i++) {
                    stmt.setObject(i + 1, orderedParams.get(i));
                }
                ResultSet rs = stmt.executeQuery();
                return ResultSetMapper.mapToList(rs, clazz);
            }
        } catch (SQLException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Query failed", e);
        }
    }
}
