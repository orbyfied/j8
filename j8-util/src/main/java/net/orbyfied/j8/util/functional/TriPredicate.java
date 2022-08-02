package net.orbyfied.j8.util.functional;

@FunctionalInterface
public interface TriPredicate<A, B, C> {

    boolean test(A a, B b, C c);

}
