package net.orbyfied.j8.util.math.expr;

public enum Operator {

    PLUS("+"),
    MINUS("-"),
    DIVIDE("/"),
    MULTIPLY("*"),
    POW("^");

    String string;

    Operator(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

}
