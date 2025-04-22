package io.github.foreverstr.util;

import io.github.foreverstr.exception.ErrorCode;
import io.github.foreverstr.exception.JormException;

public class AssertUtils {
    //断言方法，处理简单的非空异常
    public static void throwAway(Object obj, ErrorCode code) {
        if (obj == null) {
            throw new JormException(code);
        }
    }
}
