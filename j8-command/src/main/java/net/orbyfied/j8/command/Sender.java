package net.orbyfied.j8.command;

/**
 * A command sender of any internal type,
 * based on the implementation.
 */
public interface Sender {

    boolean hasPermission(String perm);

    default boolean is(Class<?> klass) {
        return klass.isInstance(as());
    }

    @SuppressWarnings("unchecked")
    default <T> T as() {
        return (T) unwrap();
    }

    Object unwrap();

}
