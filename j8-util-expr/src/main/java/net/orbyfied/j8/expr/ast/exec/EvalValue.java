package net.orbyfied.j8.expr.ast.exec;

import net.orbyfied.j8.expr.error.ExprInterpreterException;

import java.util.function.Function;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class EvalValue<V> {

    public static EvalValue<String> string(String str) {
        return new EvalValue<>(TYPE_STRING, str);
    }

    public static EvalValue<Double> number(double d) {
        return new EvalValue<>(TYPE_NUMBER, d);
    }

    /////////////////////////////////////////

    public static class Type<T> {

        // the type name
        private final String name;
        // the type id
        private final byte id;
        // the java type it wraps
        // only for AST evaluation
        // unused with the VM
        private final Class<?> wraps;
        // the to string function
        private Function<T, String> toString;

        Type(String name, int id, Class<T> wraps) {
            this(name, (byte)id, wraps);
        }

        Type(String name, byte id, Class<T> wraps) {
            this.name  = name;
            this.id    = id;
            this.wraps = wraps;
        }

        public byte id() {
            return id;
        }

        public Class<?> wraps() {
            return wraps;
        }

        public Type<T> setToString(Function<T, String> toString) {
            this.toString = toString;
            return this;
        }

    }

    // primitive value types
    public static final Type<?> TYPE_NIL              = new Type<>("nil", 0, Void.class)
            .setToString(__ -> "nil");
    public static final Type<?> TYPE_INTERNAL_FRAME   = new Type<>("frame", 1, /* TODO */ Void.class)
            .setToString(frame -> "<unsupported>");
    public static final Type<?> TYPE_NUMBER           = new Type<>("number", 2, Double.class)
            .setToString(aDouble -> Double.toString(aDouble));
    public static final Type<?> TYPE_STRING           = new Type<>("string", 3, String.class)
            .setToString(str -> "\"" + str + "\"");
    public static final Type<?> TYPE_OBJECT_REFERENCE = new Type<>("reference", 4, Long.class)
            .setToString(aLong -> "obj" + Long.toHexString(aLong)); // holds all arrays, lists, tables and instances
    public static final Type<?> TYPE_FUNCTION         = new Type<>("function", 5, EvalFunction.class)
            .setToString(evalFunction -> "function");

    // nil value
    public static final EvalValue<?> NIL = new EvalValue<>(TYPE_NIL, null);

    ////////////////////////////////////////////////////////////////

    // the type of value
    Type type;
    // the value
    V value;

    public EvalValue(Type type) {
        this.type  = type;
        this.value = null;
    }

    public EvalValue(Type type, V value) {
        this.type  = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public V getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValueAs() {
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValueAs(Class<T> tClass) {
        return (T) value;
    }

    public EvalValue<V> requireType(Type type) {
        if (this.type != type)
            throw new ExprInterpreterException("invalid value: expected " + type.name + " but got " + this.type.name);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        return (String) type.toString.apply(value);
    }

}
