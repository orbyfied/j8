package net.orbyfied.j8.util.math.expr;

public class Token<T> {

    public enum Type {

        NUMBER_LITERAL("number_literal", Double.TYPE, true),

        OPERATOR("operator", Operator.class, true),

        LEFT_PARENTHESIS("left_paren", null, false),
        RIGHT_PARENTHESIS("right_paren", null, false),
        COMMA("comma", null, false),
        DOT("dot", null, false),
        ASSIGN("assign", null, false),

        IDENTIFIER("identifier", String.class, true),

        KW_FUNC("keyword_func", null, false);

        // the name of this token
        final String name;
        // the type of value this token
        // will hold, optional
        final Class<?> valType;
        // if a value is required
        boolean reqVal;

        Type(String name, Class<?> valType, boolean reqVal) {
            this.name    = name;
            this.valType = valType;
            this.reqVal  = reqVal;
        }

        public String getName() {
            return name;
        }

        public Class<?> getValueType() {
            return valType;
        }

        public boolean requiresValue() {
            return reqVal;
        }

    }

    /////////////////////////////

    // the type of this token
    final Type type;
    // the value, optional if
    // the type of this token
    // does not require a value
    T value;
    // the location in the string
    StringLocation loc;

    // internal
    boolean hasVal = false;

    public Token(Type type, T value) {
        this(type);
        setValue(value);
    }

    public Token(Type type) {
        this.type = type;
    }

    public Token<T> located(StringLocation loc) {
        this.loc = loc;
        return this;
    }

    public StringLocation getLocation() {
        return loc;
    }

    public Type getType() {
        return type;
    }

    public T getValueUnchecked() {
        return value;
    }

    public boolean hasValue() {
        return hasVal;
    }

    public Token<T> setValue(T val) {
        this.hasVal = true;
        this.value  = val;
        return this;
    }

    public Token<T> unsetValue() {
        this.hasVal = false;
        this.value  = null;
        return this;
    }

    public T getValue() {
        if (type.requiresValue() && type.getValueType() != null && !hasVal)
            throw new IllegalStateException("expected value from token of type '" + type.getName() + "' , got nil");
        return value;
    }

    @SuppressWarnings("unchecked")
    public <V> V getValueAs() {
        return (V) getValue();
    }

    @SuppressWarnings("unchecked")
    public <V> V getValueAs(Class<V> vClass) {
        return (V) getValue();
    }

    //////////////

    @Override
    public String toString() {
        return "tk" + type.name().toUpperCase() +
                (hasVal ? ": " + value : "");
    }

}
