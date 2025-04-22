package io.github.foreverstr.exception;

/**
 * <p>错误码枚举类</p>
 * <p>错误码格式：前两位表示模块，后四位表示具体错误</p>
 * <p> 10xxx：SQL执行错误</p>
 * <p> 20xxx：会话相关错误</p>
 * <p> 30xxx：事务相关错误</p>
 * <p> 40xxx：SQL构建错误</p>
 * @author 杜玉杰
 * @version 1.0
 */
public enum ErrorCode {

    // SQL执行错误（10xxx）
    RESULT_MAPPING_FAILED("10001","结果映射失败" ),
    UNKNOWN_QUERY_ERROR("10002","未知查询错误" ),
    MODEL_NOT_SPECIFIED("10003","模型未指定" ),
    INVALID_ENTITY("10004","实体无效" ),
    REFLECTION_ACCESS_FAILED("10005","反射访问失败" ),
    DELETE_EXECUTION_FAILED("10006", "删除执行失败"),
    BATCH_DELETE_FAILED("10007","批量删除失败" ),
    CONDITIONAL_DELETE_FAILED("10008","条件删除失败" ),
    UPDATE_FIELD_EMPTY("10009","更新字段为空" ),
    CONDITION_NOT_SPECIFIED("10010","条件未指定" ),
    UPDATE_EXECUTION_FAILED("10011","更新执行失败" ),
    TYPE_MISMATCH("10012","类型不匹配" ),
    INVALID_COLUMN("10013","当前列无效" ),
    PARAMETER_BINDING_FAILED("10014","参数绑定失败" ),
    DUPLICATE_KEY("10015", "主键冲突"),

    // 会话相关错误（20xxx）
    SESSION_HAS_CLOSED("20001","会话已关闭" ),
    SESSION_CLOSED_FAILED("20002","会话关闭失败" ),
    CONNECTION_ERROR("20003", "数据库连接错误"),

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

    // SQL构建错误（40xxx）
    QUERY_EXECUTION_FAILED("40001", "SQL 执行失败"),
    SQL_GENERATION_FAILED("40002", "SQL 生成失败"),
    INVALID_SELECT_CLAUSE("40003", "无效的 SELECT 子句"),
    SQL_EXECUTION_FAILED("40004","SQL 执行失败" ),
    INVALID_OPERATOR("40005", "操作符无效"),
    INVALID_ORDER_DIRECTION("40006","排序方向无效");

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
