package org.example.util;

import org.example.Enum.ErrorCode;
import org.example.core.JormException;

public class AssertUtils {
    public static void throwAway(Object obj, ErrorCode code) {
        if (obj == null) {
            throw new JormException(code);
        }
    }
}
