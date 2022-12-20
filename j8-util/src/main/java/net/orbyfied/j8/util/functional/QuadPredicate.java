package net.orbyfied.j8.util.functional;

@FunctionalInterface
public interface QuadPredicate<A, B, C, D> {

    boolean test(A a, B b, C c, D d);

}
