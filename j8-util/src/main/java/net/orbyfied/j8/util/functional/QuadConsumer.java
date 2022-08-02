package net.orbyfied.j8.util.functional;

@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {

    void accept(A a, B b, C c, D d);

}
