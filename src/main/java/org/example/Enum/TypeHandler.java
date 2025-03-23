package org.example.Enum;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum TypeHandler {
    INTEGER(int.class, Integer.class) {
        @Override Object getValue(ResultSet rs, String col) throws SQLException { return rs.getInt(col); }
        @Override Object getNullValue(Class<?> type) { return type.isPrimitive() ? 0 : null; }
    },
    LONG(long.class, Long.class) {
        @Override Object getValue(ResultSet rs, String col) throws SQLException { return rs.getLong(col); }
        @Override Object getNullValue(Class<?> type) { return type.isPrimitive() ? 0L : null; }
    },
    BOOLEAN(boolean.class, Boolean.class) {
        @Override Object getValue(ResultSet rs, String col) throws SQLException { return rs.getBoolean(col); }
        @Override Object getNullValue(Class<?> type) { return type.isPrimitive() ? false : null; }
    },
    SHORT(short.class, Short.class) {
        @Override Object getValue(ResultSet rs, String col) throws SQLException { return rs.getShort(col); }
        @Override Object getNullValue(Class<?> type) { return type.isPrimitive() ? (short) 0 : null; }
    },
    STRING(String.class) {
        @Override Object getValue(ResultSet rs, String col) throws SQLException { return rs.getString(col); }
        @Override Object getNullValue(Class<?> type) { return null; }
    },
    DOUBLE(double.class, Double.class) {
        @Override Object getValue(ResultSet rs, String col) throws SQLException { return rs.getDouble(col); }
        @Override Object getNullValue(Class<?> type) { return type.isPrimitive() ? 0.0 : null; }
    },
    DATE(java.util.Date.class) {
        @Override Object getValue(ResultSet rs, String col) throws SQLException { return rs.getTimestamp(col); }
        @Override Object getNullValue(Class<?> type) { return null; }
    };

    private final Class<?>[] supportedTypes;
    private static final Map<Class<?>, TypeHandler> handlerCache = new ConcurrentHashMap<>();
    TypeHandler(Class<?>... supportedTypes) {
        this.supportedTypes = supportedTypes;
    }
    static {
        for (TypeHandler handler : TypeHandler.values()) {
            for (Class<?> type : handler.supportedTypes) {
                handlerCache.put(type, handler);
            }
        }
    }
    public static TypeHandler forType(Class<?> type) {
        return handlerCache.get(type);
    }

    abstract Object getValue(ResultSet rs, String col) throws SQLException;
    abstract Object getNullValue(Class<?> type);

    public Object handle(ResultSet rs, String col, Class<?> type) throws SQLException {
        Object value = getValue(rs, col);
        return rs.wasNull() ? getNullValue(type) : value;
    }
}
