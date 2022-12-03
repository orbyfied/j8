package net.orbyfied.j8.expr.ast;

public enum ASTNodeType {

    // constants
    CONSTANT("constant"),
    LOCAL_SCOPE("local_scope"),

    // operations
    BIN_OP("bin_op"),
    UNARY_OP("unary_op"),

    // objects
    INDEX("index"),
    CALL("call"),

    ;

    String name;

    ASTNodeType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
