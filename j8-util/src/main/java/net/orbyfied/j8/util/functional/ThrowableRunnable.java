package net.orbyfied.j8.util.functional;

@FunctionalInterface
public interface ThrowableRunnable {

    void run() throws Throwable;

}
