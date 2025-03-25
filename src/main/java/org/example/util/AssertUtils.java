package org.example.util;

import org.example.exception.ErrorCode;
import org.example.exception.JormException;

public class AssertUtils {
    /**
     * 断言方法，处理简单的非空异常
     */
    public static void throwAway(Object obj, ErrorCode code) {
        if (obj == null) {
            throw new JormException(code);
        }
    }
}
