package net.orbyfied.j8.util.functional;

@FunctionalInterface
public interface QuadFunction<A, B, C, D, R> {

    R apply(A a, B b, C c, D d);

}