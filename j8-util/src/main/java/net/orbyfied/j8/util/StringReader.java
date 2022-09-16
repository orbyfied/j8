package net.orbyfied.j8.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A class to help with reading/parsing
 * strings.
 */
public class StringReader {

    private static final Set<Character> DIGITS_2  = Set.of('0', '1');
    private static final Set<Character> DIGITS_8  = Set.of('0', '1', '2', '3', '4', '5', '6', '7');
    private static final Set<Character> DIGITS_10 = Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
    private static final Set<Character> DIGITS_16 = Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F');

    /**
     * Character to indicate EOF.
     */
    public static final char DONE = '\uFFFF';

    // the current index
    private int index = 0;
    // the string to read
    private String str;
    // the total string length
    private int len;

    /**
     * Constructor.
     * Creates a new string reader for the provided
     * string from the provided index.
     * @param str The string.
     * @param index The index.
     */
    public StringReader(String str, int index) {
        this.str   = str;
        this.len   = str.length();
        this.index = index;
    }

    /**
     * @see StringReader#StringReader(String, int)
     * Parameter {@code index} is defaulted to 0.
     */
    public StringReader(String str) {
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
     * Get a character from the string by
     * index. Clamped.
     * @param i The index.
     * @return The character.
     */
    public char peekAt(int i) {
        return str.charAt(clamp(i));
    }

    /**
     * Get a character from the string relative
     * to the current index. Not clamped, rather
     * returns {@link StringReader#DONE} if the
     * index is in an invalid position.
     * @param i The index.
     * @return The character or {@link StringReader#DONE}
     */
    public char peek(int i) {
        int idx = index + i;
        if (idx < 0 || idx >= len)
            return DONE;
        return str.charAt(idx);
    }

    /**
     * Advances the position by 1 and
     * returns the character or {@link StringReader#DONE}
     * if in an invalid position.
     * @return The character.
     */
    public char next() {
        if ((index += 1) >= len || index < 0) return DONE;
        return str.charAt(index);
    }

    /**
     * Advances the position by {@code a} and
     * returns the character or {@link StringReader#DONE}
     * if in an invalid position.
     * @param a The amount to advance by.
     * @return The character.
     */
    public char next(int a) {
        if ((index += a) >= len || index < 0) return DONE;
        return str.charAt(index);
    }

    /**
     * Decreases the position by 1 and
     * returns the character or {@link StringReader#DONE}
     * if in an invalid position.
     * @return The character.
     */
    public char prev() {
        if ((index -= 1) >= len || index < 0) return DONE;
        return str.charAt(index);
    }

    /**
     * Decreases the position by {@code a} and
     * returns the character or {@link StringReader#DONE}
     * if in an invalid position.
     * @param a The amount to decrease by.
     * @return The character.
     */
    public char prev(int a) {
        if ((index -= a) >= len || index < 0) return DONE;
        return str.charAt(index);
    }

    /**
     * Returns the character at the current position
     * or {@link StringReader#DONE} if in an invalid position.
     * @return The character.
     */
    public char current() {
        if (index < 0 || index >= len) return DONE;
        return str.charAt(index);
    }

    // predicate to always return true
    private static final Predicate<Character> ALWAYS = c -> true;

    /**
     * Collects until the end of the string.
     * @return The string.
     */
    public String collect() {
        return collect(ALWAYS, null);
    }

    public String collect(Predicate<Character> pred, int offEnd) {
        String str = collect(pred);
        next(offEnd);
        return str;
    }

    public String collect(Predicate<Character> pred) {
        return collect(pred, null);
    }

    public String collect(Predicate<Character> pred, Predicate<Character> skip, int offEnd) {
        String str = collect(pred, skip);
        next(offEnd);
        return str;
    }

    public String collect(Predicate<Character> pred, Predicate<Character> skip) {
        return collect(pred, skip, null);
    }

    public String collect(Predicate<Character> pred, Predicate<Character> skip, Consumer<Character> charEval) {
        if (pred == null)
            pred = ALWAYS;
        StringBuilder b = new StringBuilder();
        prev();
        char c;
        while ((c = next()) != DONE) {
            boolean sf = skip != null && skip.test(c);
            if (!sf) {
                if (!pred.test(c))
                    break;

                if (charEval != null)
                    charEval.accept(c);
                b.append(c);
            }
        }

        return b.toString();
    }

    public String pcollect(Predicate<Character> pred) {
        return pcollect(pred, null);
    }

    public String pcollect(Predicate<Character> pred, Predicate<Character> skip) {
        if (pred == null)
            pred = ALWAYS;
        StringBuilder b = new StringBuilder();
        int off = 0;
        char c;
        while ((c = peek(off++)) != DONE && pred.test(c)) {
            if (skip == null || !skip.test(c)) {
                b.append(c);
            }
        }

        return b.toString();
    }

    public List<String> split(char... chars) {
        List<String> list = new ArrayList<>(len / 10);
        HashSet<Character> charSet = new HashSet<>();
        for (char c : chars)
            charSet.add(c);
        while (current() != DONE) {
            list.add(collect(c -> !charSet.contains(c)));
            next();
        }
        return list;
    }

    public int index() {
        return index;
    }

    public StringReader index(int i) {
        this.index = i;
        return this;
    }

    public String getString() {
        return str;
    }

    public StringReader subForward(int from, int len) {
        StringReader reader = new StringReader(str, from);
        reader.len = Math.min(from + len, this.len - from);
        return reader;
    }

    public StringReader subFrom(int from, int len) {
        StringReader reader = new StringReader(str, index + from);
        reader.len = Math.min(from + len, this.len - from - index);
        return reader;
    }

    public StringReader branch() {
        return new StringReader(str, index);
    }

    /* ----- Utilities ----- */

    public boolean isDigit(char c, int radix) {
        char c1 = Character.toUpperCase(c);
        return switch (radix) {
            case 2 -> DIGITS_2.contains(c1);
            case 8 -> DIGITS_8.contains(c1);
            case 10 -> DIGITS_10.contains(c1);
            case 16 -> DIGITS_16.contains(c1);
            default -> false;
        };
    }

    public int collectInt(final int radix) {
        String str = collect(c -> isDigit(c, radix));
        return Integer.parseInt(str);
    }

}
