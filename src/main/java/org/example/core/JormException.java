package org.example.core;

import org.example.Enum.ErrorCode;

/**
 * JORM 框架统一异常，封装所有数据库操作相关的错误
 */
public class JormException extends RuntimeException {
    private final ErrorCode errorCode;

    public JormException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public JormException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public JormException(ErrorCode errorCode, String detailMessage, Throwable cause) {
        super(errorCode.getMessage() + ": " + detailMessage, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
