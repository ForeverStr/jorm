package io.github.foreverstr.session;

import io.github.foreverstr.cache.SecondLevelCache;
import io.github.foreverstr.dto.Condition;
import io.github.foreverstr.session.base.BaseSession;
import io.github.foreverstr.sqlBuilder.FindBuilder;
import io.github.foreverstr.util.ResultSetMapper;
import io.github.foreverstr.exception.ErrorCode;
import io.github.foreverstr.exception.JormException;
import io.github.foreverstr.cache.CacheManager;
import io.github.foreverstr.util.EntityHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 该类继承自 {@link BaseSession}，实现了查询逻辑的相关操作,必须通过 try-with-resources 使用本类
 * @author duyujie
 * @version 1.0
 * @see BaseSession
 * @see FindBuilder
 * @see ResultSetMapper
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
     * <p>
     *     链式添加select子句
     * </p>
     * <p>
     *     该方法用于指定查询结果中需要包含的字段，默认值为“*”，即查询所有字段。
     * </p>
     * @param selectClause 要查询的字段列表，以逗号分隔。
     * @return 当前对象，用于链式调用。
     */
    public FindSession Select(String selectClause) {
        this.selectClause = selectClause;
        return self();
    }
    /**
     * <p>
     *     链式添加Having子句
     * </p>
     * <p>
     *     该方法用于指定查询结果中需要进行分组的字段，以及分组条件。
     * </p>
     * @param column 字段名。
     * @param operator 操作符。
     * @param value 字段值。
     * @return 当前对象，用于链式调用。
     */
    public FindSession Having(String column, String operator, Object value) {
        havingConditions.add(new Condition(column, operator, value));
        params.add(value);
        return self();
    }
    /**
     * <p>
     *     链式添加分组子句
     * </p>
     * <p>
     *     该方法用于指定查询结果中需要进行分组的字段。
     * </p>
     * @param group 字段名。
     * @return 当前对象，用于链式调用。
     */
    public FindSession Group(String group){
        this.group = group;
        return self();
    }
    /**
     * <p>
     *     链式添加等值条件
     * </p>
     * <p>
     *     该方法用于指定查询条件，等值条件。
     * </p>
     * @param column 字段名。
     * @param value 字段值。
     * @return 当前对象，用于链式调用。
     */
    public FindSession Where(String column, Object value) {
        conditions.add(new Condition(column, "=", value));
        params.add(value);
        return self();
    }
    /**
     * <p>
     *     链式添加条件
     * </p>
     * <p>
     *     该方法用于指定查询条件。
     * </p>
     * @param column 字段名。
     * @param operator 操作符。
     * @param value 字段值。
     * @return 当前对象，用于链式调用。
     */
    public FindSession Where(String column, String operator, Object value) {
        conditions.add(new Condition(column, operator, value));
        params.add(value);
        return self();
    }
    /**
     * <p>
     *     链式添加Limit子句
     * </p>
     * <p>
     *     该方法用于指定查询结果的数量限制。
     * </p>
     * @param limit 限制数量。
     * @return 当前对象，用于链式调用。
     */
    public FindSession Limit(Integer limit){
        this.limit = limit;
        return self();
    }
    /**
     * <p>
     *     链式添加Order子句
     * </p>
     * <p>
     *     该方法用于指定查询结果的排序方式。
     * </p>
     * @param orderBy 排序字段。
     * @return 当前对象，用于链式调用。
     */
    public FindSession Order(String orderBy){
        this.orderBy = orderBy;
        return self();
    }
    /**
     * <p>
     *     执行查询
     * </p>
     * <p>
     *     该方法用于执行查询操作，并返回结果。
     * </p>
     * @param clazz 要查询的模型类。
     * @param <T> 模型类型。
     * @return 查询结果。
     */
    public <T> List<T> Find(Class<T> clazz) {
        // 前置校验：确保连接和参数合法
        checkIfClosed();
        if (clazz == null) {
            log.error("[ErrorCode={}] 模型未指定", ErrorCode.MODEL_NOT_SPECIFIED.getCode());
            throw new JormException(ErrorCode.MODEL_NOT_SPECIFIED, "模型未指定");
        }
        // 生成缓存键
        String cacheKey = generateCacheKey(clazz, conditions, limit, orderBy, group, havingConditions, selectClause);
        // 尝试从二级缓存获取
        if (CacheManager.isCacheEnabled()) {
            SecondLevelCache cache = CacheManager.getSecondLevelCache();
            Object cachedResult = cache.get(clazz.getName(), cacheKey);
            if (cachedResult != null) {
                log.debug("从二级缓存获取数据: [Class={}, Key={}]", clazz.getName(), cacheKey);
                return (List<T>) cachedResult;
            }
        }
        String sql = null;
        try {
            sql = FindBuilder.buildFindSelect(clazz, conditions, limit, orderBy, group, havingConditions, selectClause);
            log.debug("生成的SQL: [{}], 参数: {}", sql, params);

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                ResultSet rs = stmt.executeQuery();
                List<T> result = ResultSetMapper.mapToList(rs, clazz);
                // 将结果放入二级缓存
                if (CacheManager.isCacheEnabled() && result != null && !result.isEmpty()) {
                    SecondLevelCache cache = CacheManager.getSecondLevelCache();
                    cache.put(clazz.getName(), cacheKey, result);
                    log.debug("数据已缓存: [Class={}, Key={}, Size={}]", clazz.getName(), cacheKey, result.size());
                }
                return result;
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
    private String generateCacheKey(Class<?> clazz, List<Condition> conditions, Integer limit,
                                    String orderBy, String group, List<Condition> havingConditions,
                                    String selectClause) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("select:").append(selectClause);

        if (conditions != null && !conditions.isEmpty()) {
            keyBuilder.append(":where:");
            for (Condition condition : conditions) {
                keyBuilder.append(condition.getColumn()).append(condition.getOperator());
                if (condition.getValue() != null) {
                    keyBuilder.append(condition.getValue().toString());
                }
                keyBuilder.append(";");
            }
        }

        if (group != null) {
            keyBuilder.append(":group:").append(group);
        }

        if (havingConditions != null && !havingConditions.isEmpty()) {
            keyBuilder.append(":having:");
            for (Condition condition : havingConditions) {
                keyBuilder.append(condition.getColumn()).append(condition.getOperator());
                if (condition.getValue() != null) {
                    keyBuilder.append(condition.getValue().toString());
                }
                keyBuilder.append(";");
            }
        }

        if (orderBy != null) {
            keyBuilder.append(":order:").append(orderBy);
        }

        if (limit != null) {
            keyBuilder.append(":limit:").append(limit);
        }

        return keyBuilder.toString();
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
