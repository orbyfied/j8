package net.orbyfied.j8.util.functional;

public interface KeyProvider<K> {

    void provideKeys(Accumulator<K> acc);

}
