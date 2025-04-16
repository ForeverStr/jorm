package org.example.exception;

/**
 * 异常枚举类
 * @author 杜玉杰
 */
public enum ErrorCode {
    CONNECTION_FAILED("ORM-001", "数据库连接失败"),
    SQL_GENERATION_FAILED("ORM-002", "SQL 生成失败"),
    TRANSACTION_FAILED("ORM-003", "事务操作失败"),
    QUERY_EXECUTION_FAILED("ORM-004", "SQL 执行失败"),
    RESULT_MAPPING_FAILED("ORM-005", "结果集映射失败"),
    TYPE_MISMATCH("ORM-006","类型不匹配" ),
    INVALID_COLUMN_NAME("ORM-007","无效的列名"),
    INVALID_COLUMN("ORM-008","当前列无效" ),
    INVALID_OPERATOR("ORM-009", "操作符无效"),
    INVALID_ORDER_DIRECTION("ORM-0010","排序方向无效"),
    INVALID_SELECT_CLAUSE("ORM-0011", "无效的 SELECT 子句"),
    CONNECTION_ERROR("ORM-0012", "数据库连接错误"),
    TRANSACTION_ALREADY_ACTIVE("ORM-0013","事务已经处于活动状态" ),
    COMMIT_FAILED("ORM-0014","提交事务失败" ),
    ROLLBACK_FAILED("ORM-0015","回滚事务失败" ),
    NESTED_TRANSACTION_NOT_SUPPORTED("ORM-0016","嵌套事务不支持" ),
    NO_ACTIVE_TRANSACTION("ORM-0017","没有活动事务" ),
    SAVEPOINT_FAILED("ORM-0018","创建保存点失败" ),
    NO_SAVEPOINT("ORM-0019","没有保存点" ),
    SESSION_CLOSED("ORM-0020","会话已关闭" ),
    DUPLICATE_SAVEPOINT_NAME("ORM-0021","保存点名称重复" ),
    QUERY_FAILED("ORM-0022","查询失败" ),
    DELETE_ERROR("ORM-0023","删除失败" ),
    SQL_EXECUTION_FAILED("ORM-0024","SQL 执行失败" ),
    PARAMETER_BINDING_FAILED("ORM-0025","参数绑定失败" ),
    MODEL_NOT_SPECIFIED("ORM-0026","模型未指定" ),
    CONDITION_NOT_SPECIFIED("ORM-0027","条件未指定" ),
    DUPLICATE_KEY("ORM-0028", "主键冲突"),
    CONNECTION_TIMEOUT("ORM-0029", "数据库连接超时"),;
    private final String code;
    private final String message;
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
