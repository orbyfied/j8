package net.orbyfied.j8.util.math.expr;

public abstract class ExpressionNode {

    public enum Type {

        /**
         * For a constant value.
         */
        CONSTANT,

        /**
         * Binary operator.
         */
        BIN_OP,

        /**
         * Unary operator.
         */
        UNARY_OP,

        /**
         * Index.
         */
        INDEX,

        /**
         * Assign.
         */
        ASSIGN,

        /**
         * Call value.
         */
        CALL;

    }

    /////////////////////////////

    public ExpressionNode(Type type) {
        this.type = type;
    }

    // the node type
    final Type type;

    public Type getType() {
        return type;
    }

    public abstract ExpressionValue<?> evaluate(Context context);

    ////////////////////////////////

    protected String getDataAsString() {
        return "";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getDataAsString();
    }

}
