package net.orbyfied.j8.util.functional;

@FunctionalInterface
public interface ThrowableSupplier<T> {

    T get() throws Throwable;

}
