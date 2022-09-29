package net.orbyfied.j8.util.math.expr;

public abstract class ExpressionNode {

    public enum Type {

        /**
         * For a constant value.
         */
        CONSTANT("Constant"),

        /**
         * Binary operator.
         */
        BIN_OP("BinOp"),

        /**
         * Unary operator.
         */
        UNARY_OP("UnaryOp"),

        /**
         * Index.
         */
        INDEX("Index"),

        /**
         * Assign.
         */
        ASSIGN("Assign"),

        /**
         * Call value.
         */
        CALL("Call"),

        /**
         * If statement.
         */
        IF("If"),

        /**
         * Miscellaneous.
         */
        MISC("Misc");

        protected String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    /////////////////////////////

    public ExpressionNode(Type type) {
        this.type = type;
    }

    // the node type
    final Type type;
    // the location, optional
    StringLocation loc = StringLocation.EMPTY;

    public Type getType() {
        return type;
    }

    public ExpressionNode located(StringLocation loc) {
        this.loc = loc;
        return this;
    }

    public StringLocation getLocation() {
        return loc;
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
