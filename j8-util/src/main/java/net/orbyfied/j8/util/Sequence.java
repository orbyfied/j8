package net.orbyfied.j8.util;

import java.util.List;

/**
 * A sequence of data.
 * @param <T> The type of the data.
 */
public interface Sequence<T> {

    T at(int i);

    int size();

    ////////////////////////////////

    static Sequence<Character> ofString(final String str) {
        return new Sequence<>() {
            @Override
            public Character at(int i) {
                return str.charAt(i);
            }

            @Override
            public int size() {
                return str.length();
            }
        };
    }

    static <E> Sequence<E> ofList(final List<E> list) {
        return new Sequence<>() {
            @Override
            public E at(int i) {
                return list.get(i);
            }

            @Override
            public int size() {
                return list.size();
            }
        };
    }

}
