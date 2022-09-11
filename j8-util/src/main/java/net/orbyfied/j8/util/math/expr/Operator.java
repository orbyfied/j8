package net.orbyfied.j8.util.math.expr;

public enum Operator {

    PLUS("+"),
    MINUS("-"),
    DIVIDE("/"),
    MULTIPLY("*"),
    POW("^");

    String string;
    int parameterCount;

    Operator(String string) {
        this.string = string;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public String getString() {
        return string;
    }

}
