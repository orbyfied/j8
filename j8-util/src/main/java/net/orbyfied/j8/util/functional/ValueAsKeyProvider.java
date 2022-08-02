package net.orbyfied.j8.util.functional;

public interface ValueAsKeyProvider<A> extends KeyProvider<A>, ValueProvider<A> {

    @Override
    default void provideKeys(Accumulator<A> acc) {
        provideValues(acc);
    }

}
