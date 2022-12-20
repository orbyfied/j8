package net.orbyfied.j8.util.functional;

import java.util.function.Function;

public interface Accumulator<T> {

    void add(T item);

    /////////////////////////////////////

    static <S, D> Accumulator<S> mapped(Accumulator<D> destination,
                                        Function<S, D> mapper) {
        return item -> destination.add(mapper.apply(item));
    }

}
