package net.orbyfied.j8.util.math.expr.vm;

public record StackExprInst(short opcode) {

    /*
        Opcodes
     */

    // stack operations
    public static final short OP_PUSH_NUMBER = 0x010;
    // pops an item off the stack and discards it
    public static final short OP_VOID_POP = 0x012;

    // arithmetic
    public static final short OP_ADD = 0x10;
    public static final short OP_SUB = 0x11;
    public static final short OP_MUL = 0x12;
    public static final short OP_DIV = 0x13;
    public static final short OP_POW = 0x14;

}
