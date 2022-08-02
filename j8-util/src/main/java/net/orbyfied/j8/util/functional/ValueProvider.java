package net.orbyfied.j8.util.functional;

public interface ValueProvider<V> {

    void provideValues(Accumulator<V> acc);

}
