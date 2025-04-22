package io.github.foreverstr.dto;

public class Condition {

    private final String column;
    private final String operator;
    private final Object value;

    public Condition(String column, String operator, Object value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

    public String getColumn() {
        return column;
    }

    public String getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

}
