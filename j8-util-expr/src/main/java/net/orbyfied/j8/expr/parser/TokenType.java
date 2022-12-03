package net.orbyfied.j8.expr.parser;

public enum TokenType {

    // value literals
    NUMBER_LITERAL(true),
    STRING_LITERAL(true),

    // operator
    OPERATOR(true),

    // identifier
    IDENTIFIER(true),

    // keywords
    K_RETURN(false),

    // special
    LEFT_PAREN(false),
    RIGHT_PAREN(false),
    LEFT_BRACKET(false),
    RIGHT_BRACKET(false),
    DOT(false),
    COMMA(false),

    ;

    // if tokens of this type should
    // hold a value
    private final boolean valued;

    TokenType(boolean valued) {
        this.valued = valued;
    }

    public boolean isValued() {
        return valued;
    }

}
