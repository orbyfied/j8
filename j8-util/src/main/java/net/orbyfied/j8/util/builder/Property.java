package net.orbyfied.j8.util.builder;

import java.util.Objects;

public class Property<T> {

    public static <T> Property<T> of(Class<T> t) {
        return new Property<>(t);
    }

    public static Property<String> ofString() {
        return of(String.class);
    }

    public static Property<Boolean> ofBool() {
        return of(Boolean.class);
    }

    public static Property<Byte> ofByte() {
        return of(Byte.class);
    }

    public static Property<Character> ofChar() {
        return of(Character.class);
    }

    public static Property<Short> ofShort() {
        return of(Short.class);
    }

    public static Property<Integer> ofInt() {
        return of(Integer.class);
    }

    public static Property<Long> ofLong() {
        return of(Long.class);
    }

    public static Property<Float> ofFloat() {
        return of(Float.class);
    }

    public static Property<Double> ofDouble() {
        return of(Double.class);
    }

    ///////////////////////////////////////

    // the runtime type
    protected final Class<T> type;

    // the name
    protected String name;

    // defaults
    protected boolean isDefaulted;
    protected T defaultValue;

    // required
    protected boolean isRequired;

    /**
     * Constructor.
     * @param type The runtime type.
     */
    Property(Class<T> type) {
        Objects.requireNonNull(type, "type cannot be null");
        this.type = type;
    }

    /**
     * Builder-take constructor.
     */
    Property(Builder<Property<?>, ?> builder) {
        this.type = null;
    }

    /* Getter. */

    public Class<T> type() {
        return type;
    }

    public String named() {
        return name;
    }

    public boolean required() {
        return isRequired;
    }

    public boolean defaulted() {
        return isDefaulted;
    }

    public T defaultValue() {
        return defaultValue;
    }

    /* Setter. */

    public Property<T> named(String name) {
        this.name = name;
        return this;
    }

    public Property<T> defaulted(T val) {
        this.isDefaulted  = true;
        this.defaultValue = val;
        return this;
    }

    public Property<T> require(boolean bool) {
        this.isRequired = bool;
        return this;
    }

}
