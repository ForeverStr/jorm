package org.example.exception;

/**
 * 异常枚举类
 * @author 杜玉杰
 */
public enum ErrorCode {
    SQL_GENERATION_FAILED("ORM-002", "SQL 生成失败"),
    TYPE_MISMATCH("ORM-006","类型不匹配" ),
    INVALID_COLUMN("ORM-008","当前列无效" ),
    INVALID_OPERATOR("ORM-009", "操作符无效"),
    INVALID_ORDER_DIRECTION("ORM-0010","排序方向无效"),
    INVALID_SELECT_CLAUSE("ORM-0011", "无效的 SELECT 子句"),
    CONNECTION_ERROR("ORM-0012", "数据库连接错误"),
    QUERY_FAILED("ORM-0022","查询失败" ),
    DELETE_ERROR("ORM-0023","删除失败" ),
    SQL_EXECUTION_FAILED("ORM-0024","SQL 执行失败" ),
    PARAMETER_BINDING_FAILED("ORM-0025","参数绑定失败" ),
    CONDITION_NOT_SPECIFIED("ORM-0027","条件未指定" ),
    DUPLICATE_KEY("ORM-0028", "主键冲突"),

    // 事务相关错误（30xxx）
    TRANSACTION_BEGIN_FAILED("30001","事务开启失败"),
    TRANSACTION_COMMIT_FAILED("30002","事务提交失败" ),
    TRANSACTION_ROLLBACK_FAILED("30003","事务回滚失败" ),
    TRANSACTION_CLOSE_FAILED("30004","事务关闭失败" ),
    TRANSACTION_CLOSURE_FAILED("30005", "事务闭包异常"),
    TRANSACTION_AUTOMATIC_FAILED("30006","自动事务异常" ),
    SAVEPOINT_FAILED("30007","创建保存点失败" ),
    NO_SAVEPOINT("30008","没有保存点" ),
    ROLLBACK_FAILED("30009","回滚事务失败" ),
    DUPLICATE_SAVEPOINT_NAME("30010","保存点名称重复" ),

    // 会话相关错误（20xxx）
    SESSION_HAS_CLOSED("20001","会话已关闭" ),
    SESSION_CLOSED_FAILED("20002","会话关闭失败" ),

    // 其他错误（10xxx）
    QUERY_EXECUTION_FAILED("10001", "SQL 执行失败"),
    RESULT_MAPPING_FAILED("10002","结果映射失败" ),
    UNKNOWN_QUERY_ERROR("10003","未知查询错误" ),
    MODEL_NOT_SPECIFIED("10004","模型未指定" ),;

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
