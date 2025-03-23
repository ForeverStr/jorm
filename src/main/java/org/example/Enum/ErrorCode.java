package org.example.Enum;

public enum ErrorCode {
    CONNECTION_FAILED("ORM-001", "数据库连接失败"),
    SQL_GENERATION_FAILED("ORM-002", "SQL 生成失败"),
    TRANSACTION_FAILED("ORM-003", "事务操作失败"),
    QUERY_EXECUTION_FAILED("ORM-004", "SQL 执行失败"),
    RESULT_MAPPING_FAILED("ORM-005", "结果集映射失败"),
    TYPE_MISMATCH("ORM-006","类型不匹配" );

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
