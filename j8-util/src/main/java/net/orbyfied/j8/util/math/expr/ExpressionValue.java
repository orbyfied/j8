package net.orbyfied.j8.util.math.expr;

import net.orbyfied.j8.util.math.expr.error.ExprInterpreterException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ExpressionValue<T> {

    @SuppressWarnings("unchecked")
    public static ExpressionValue<?> of(Object val) {
        if (val == null)
            return NIL;

        if (val instanceof String) {
            return new ExpressionValue<>(Type.STRING, val);
        } else if (val instanceof Number) {
            return new ExpressionValue<>(Type.NUMBER, ((Number) val).doubleValue());
        } else if (val instanceof ExpressionFunction) {
            return new ExpressionValue<>(Type.FUNCTION, val);
        }

        return null;
    }

    public static ExpressionValue<?> ofDouble(double d) {
        return new ExpressionValue<>(Type.NUMBER, d);
    }

    /////////////////////////////////

    // the different value types
    public enum Type {

        /**
         * A 64-bit floating point number. The default type
         * for all numbers in an expression. Encapsulates
         * a {@code double}, with runtime type {@link Double#TYPE}
         */
        NUMBER("number", Double.TYPE, v -> Double.toString(v.getValueAs())),

        /**
         * A string.
         * Obviously encapsulates a {@link String}
         */
        STRING("string", String.class, v -> '"' + v.getValueAs(String.class) + '"'),

        /**
         * A key-value structure based on a hash map.
         * Obviously encapsulates a {@link HashMap}
         */
        TABLE("table", HashMap.class, v -> v.getValueAs().toString()),

        /**
         * A callable function.
         * Encapsulates an {@link ExpressionFunction}
         */
        FUNCTION("function", ExpressionFunction.class, v -> v.getValueAs().toString()),

        /**
         * A value representing nothing, the absence of
         * a value. It has a runtime type of {@link Void#TYPE}
         */
        NIL("nil", Void.TYPE, v -> "nil");

        // the type name
        String name;
        // the java runtime type
        Class<?> rtType;
        // to string function
        Function<ExpressionValue<?>, String> toString;

        Type(String name, Class<?> rtType, Function<ExpressionValue<?>, String> toString) {
            this.name     = name;
            this.rtType   = rtType;
            this.toString = toString;
        }

        public String getName() {
            return name;
        }

        public Class<?> getRuntimeType() {
            return rtType;
        }

    }

    // nil value
    public static final ExpressionValue<Void> NIL = new ExpressionValue<>(Type.NIL, null);

    ////////////////////////

    // the type
    final Type type;
    // the value
    T val;

    public ExpressionValue(Type type) {
        this.type = type;
    }

    public ExpressionValue(Type type, T val) {
        this(type);
        this.val = val;
    }

    public Type getType() {
        return type;
    }

    public T getValue() {
        return val;
    }

    @SuppressWarnings("unchecked")
    public <V> V getValueAs() {
        return (V) val;
    }

    @SuppressWarnings("unchecked")
    public <V> V getValueAs(Class<V> vClass) {
        return (V) val;
    }

    public boolean isNil() {
        return type == Type.NIL;
    }

    public ExpressionValue<T> checkNonNil() {
        if (isNil())
            throw new ExprInterpreterException("got a nil value");
        return this;
    }

    public ExpressionValue<T> checkType(Type type) {
        if (this.type != type)
            throw new ExprInterpreterException("expected " + type.name + ", got a " + this.type.name + " value");
        return this;
    }

    public void setValue(T val) {
        this.val = val;
    }

    ////////////////////////////////////////////////////

    @Override
    public String toString() {
        return type.toString.apply(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionValue<?> that = (ExpressionValue<?>) o;
        return type == that.type && Objects.equals(val, that.val);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, val);
    }

}
