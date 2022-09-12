package net.orbyfied.j8.util.math.expr;

import net.orbyfied.j8.util.math.expr.error.ExprInterpreterException;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class ExpressionValue<T> {

    /* Tables */

    public static ExpressionValue<?> newTable() {
        return new ExpressionValue<>(Type.TABLE, new HashMap<>());
    }

    public static ExpressionValue<?> tableSet(ExpressionValue<?> t, Object k, Object v) {
        t.checkType(Type.TABLE);
        t.getValueAs(HashMap.class).put(ExpressionValue.of(k), ExpressionValue.of(v));
        return t;
    }

    public static ExpressionValue<?> tableGet(ExpressionValue<?> t, Object k) {
        t.checkType(Type.TABLE);
        return (ExpressionValue<?>) t.getValueAs(HashMap.class).get(ExpressionValue.of(k));
    }

    public static boolean tableHas(ExpressionValue<?> t, Object k) {
        t.checkType(Type.TABLE);
        return t.getValueAs(HashMap.class).containsKey(ExpressionValue.of(k));
    }

    public static int tableSize(ExpressionValue<?> t) {
        t.checkType(Type.TABLE);
        return t.getValueAs(HashMap.class).size();
    }

    /* Values */

    @SuppressWarnings("unchecked")
    public static ExpressionValue<?> of(Object val) {
        if (val instanceof ExpressionValue)
            return (ExpressionValue<?>) val;

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
         * A list structure based on a Java array list.
         * Encapsulates an {@link ArrayList}
         */
        ARRAY("array", ArrayList.class, v -> v.getValueAs(ArrayList.class).toString()),

        /**
         * A callable function.
         * Encapsulates an {@link ExpressionFunction}
         */
        FUNCTION("function", ExpressionFunction.class, v -> "function"),

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

    /* -------- Structures -------- */

    public ExpressionValue<T> structIndex(ExpressionValue<?> key) {
        return switch (type) {
            case TABLE  -> (ExpressionValue<T>) getValueAs(HashMap.class).getOrDefault(key, NIL);
            case ARRAY  -> (ExpressionValue<T>) getValueAs(ArrayList.class).get(key.getValueAs(Double.class).intValue());
            case STRING -> (ExpressionValue<T>) new ExpressionValue<>(Type.STRING, "" +
                    getValueAs(String.class).charAt(key.getValueAs(Double.class).intValue()));
            default -> { throw new ExprInterpreterException("attempt to index a " + type.name + " value"); }
        };
    }

    public void structAssign(ExpressionValue<?> key, ExpressionValue<?> value) {
        switch (type) {
            case TABLE  -> getValueAs(HashMap.class).put(key, value);
            case ARRAY  -> {
                int idx = key.getValueAs(Double.class).intValue();
                ArrayList<ExpressionValue<?>> list = getValueAs(ArrayList.class);
                if (idx == list.size())
                    list.add(value);
                else
                    list.set(idx, value);
            }
            default -> { throw new ExprInterpreterException("attempt to index a " + type + " value"); }
        };
    }

    /* -------- Tables --------- */

    public ExpressionValue<T> tableSet(Object k, Object v) {
        checkType(Type.TABLE);
        getValueAs(HashMap.class).put(ExpressionValue.of(k), ExpressionValue.of(v));
        return this;
    }

    public ExpressionValue<?> tableGet(Object k) {
        checkType(Type.TABLE);
        return (ExpressionValue<?>) getValueAs(HashMap.class).getOrDefault(ExpressionValue.of(k), NIL);
    }

    public boolean tableHas(Object k) {
        checkType(Type.TABLE);
        return getValueAs(HashMap.class).containsKey(ExpressionValue.of(k));
    }

    public int tableSize() {
        checkType(Type.TABLE);
        return getValueAs(HashMap.class).size();
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
