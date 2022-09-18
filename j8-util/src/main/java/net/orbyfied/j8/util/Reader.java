package net.orbyfied.j8.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A class which sequentially reads.
 * data from a sequence.
 * @param <T> The type the sequence serves.
 */
// TODO: make Reader extend Reader<T>
public class Reader<T> {

    // the current index
    private int index = 0;
    // the string to read
    private Sequence<T> str;
    // the total string length
    private int len;

    /**
     * Constructor.
     * Creates a new string reader for the provided
     * string from the provided index.
     * @param str The string.
     * @param index The index.
     */
    public Reader(Sequence<T> str, int index) {
        this.str   = str;
        this.len   = str.size();
        this.index = index;
    }

    /**
     * @see Reader#Reader(Sequence, int)
     * Parameter {@code index} is defaulted to 0.
     */
    public Reader(Sequence<T> str) {
        this(str, 0);
    }

    /**
     * Clamps an index in to the minimum (0) and
     * maximum (string length) index.
     * @param index The index.
     * @return The clamped index.
     */
    public int clamp(int index) {
        return Math.min(len - 1, Math.max(0, index));
    }

    /**
     * Get a data from the string by
     * index. Clamped.
     * @param i The index.
     * @return The data.
     */
    public T peekAt(int i) {
        return str.at(clamp(i));
    }

    /**
     * Get a data from the string relative
     * to the current index. Not clamped, rather
     * returns {@code null} if the
     * index is in an invalid position.
     * @param i The index.
     * @return The data or {@code null}
     */
    public T peek(int i) {
        int idx = index + i;
        if (idx < 0 || idx >= len)
            return null;
        return str.at(idx);
    }

    /**
     * Advances the position by 1 and
     * returns the data or {@code null}
     * if in an invalid position.
     * @return The data.
     */
    public T next() {
        if ((index += 1) >= len || index < 0) return null;
        return str.at(index);
    }

    /**
     * Advances the position by {@code a} and
     * returns the data or {@code null}
     * if in an invalid position.
     * @param a The amount to advance by.
     * @return The data.
     */
    public T next(int a) {
        if ((index += a) >= len || index < 0) return null;
        return str.at(index);
    }

    /**
     * Decreases the position by 1 and
     * returns the data or {@code null}
     * if in an invalid position.
     * @return The data.
     */
    public T prev() {
        if ((index -= 1) >= len || index < 0) return null;
        return str.at(index);
    }

    /**
     * Decreases the position by {@code a} and
     * returns the data or {@code null}
     * if in an invalid position.
     * @param a The amount to decrease by.
     * @return The data.
     */
    public T prev(int a) {
        if ((index -= a) >= len || index < 0) return null;
        return str.at(index);
    }

    /**
     * Returns the data at the current position
     * or {@code null} if in an invalid position.
     * @return The data.
     */
    public T current() {
        if (index < 0 || index >= len) return null;
        return str.at(index);
    }

    // predicate to always return true
    private final Predicate<T> ALWAYS = c -> true;

    /**
     * Collects until the end of the string.
     * @return The string.
     */
    public String collect() {
        return collect(ALWAYS, null);
    }

    public String collect(Predicate<T> pred, int offEnd) {
        String str = collect(pred);
        next(offEnd);
        return str;
    }

    public String collect(Predicate<T> pred) {
        return collect(pred, null);
    }

    public String collect(Predicate<T> pred, Predicate<T> skip, int offEnd) {
        String str = collect(pred, skip);
        next(offEnd);
        return str;
    }

    public String collect(Predicate<T> pred, Predicate<T> skip) {
        return collect(pred, skip, null);
    }

    public String collect(Predicate<T> pred, Predicate<T> skip, Consumer<T> TEval) {
        if (pred == null)
            pred = ALWAYS;
        StringBuilder b = new StringBuilder();
        prev();
        T c;
        while ((c = next()) != null && pred.test(c)) {
            if (skip == null || !skip.test(c)) {
                if (TEval != null)
                    TEval.accept(c);
                b.append(c);
            }
        }

        return b.toString();
    }

    public String pcollect(Predicate<T> pred) {
        return pcollect(pred, null);
    }

    public String pcollect(Predicate<T> pred, Predicate<T> skip) {
        if (pred == null)
            pred = ALWAYS;
        StringBuilder b = new StringBuilder();
        int off = 0;
        T c;
        while ((c = peek(off++)) != null && pred.test(c)) {
            if (skip == null || !skip.test(c)) {
                b.append(c);
            }
        }

        return b.toString();
    }

    public List<String> split(T... Ts) {
        List<String> list = new ArrayList<>(len / 10);
        HashSet<T> TSet = new HashSet<>();
        for (T c : Ts)
            TSet.add(c);
        while (current() != null) {
            list.add(collect(c -> !TSet.contains(c)));
            next();
        }
        return list;
    }

    public int index() {
        return index;
    }

    public Reader index(int i) {
        this.index = i;
        return this;
    }

    public Sequence<T> getSequence() {
        return str;
    }

    public Reader subForward(int from, int len) {
        Reader reader = new Reader(str, from);
        reader.len = Math.min(from + len, this.len - from);
        return reader;
    }

    public Reader subFrom(int from, int len) {
        Reader reader = new Reader(str, index + from);
        reader.len = Math.min(from + len, this.len - from - index);
        return reader;
    }

    public Reader branch() {
        return new Reader(str, index);
    }

}
