package net.orbyfied.j8.util;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A class to help with reading/parsing
 * strings.
 */
public class StringReader {

    private static final int[] DIGIT_TABLE = new int[256];

    static {
        Arrays.fill(DIGIT_TABLE, -1);
        DIGIT_TABLE['0'] = 0;
        DIGIT_TABLE['1'] = 1;
        DIGIT_TABLE['2'] = 2;
        DIGIT_TABLE['3'] = 3;
        DIGIT_TABLE['4'] = 4;
        DIGIT_TABLE['5'] = 5;
        DIGIT_TABLE['6'] = 6;
        DIGIT_TABLE['7'] = 7;
        DIGIT_TABLE['8'] = 8;
        DIGIT_TABLE['9'] = 9;
        DIGIT_TABLE['a'] = 10;
        DIGIT_TABLE['b'] = 11;
        DIGIT_TABLE['c'] = 12;
        DIGIT_TABLE['d'] = 13;
        DIGIT_TABLE['e'] = 14;
        DIGIT_TABLE['f'] = 15;
    }

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

    public String escapeRemaining(Predicate<Character> toEscape,
                                  Function<Character, String> escapeFunc) {
        StringBuilder b = new StringBuilder();
        char c = current();
        while (c != DONE) {
            if (toEscape.test(c)) {
                b.append(escapeFunc.apply(c));
            } else {
                b.append(c);
            }

            c = next();
        }

        return b.toString();
    }

    /* ----- Utilities ----- */

    public void debugPrint(String s, PrintStream stream) {
        stream.println("[READER:" + s + "] current: '" + current() + "', index: " + index() + ", nv10: " + getDigit(current(), 10));
    }

    public void debugPrint(String s) {
        debugPrint(s, System.out);
    }

    public static boolean isDigit(char c, int radix) {
        return getDigit(c, radix) != -1;
    }

    public static int getDigit(char c, int radix) {
        char c1 = Character.toLowerCase(c);
        if (c1 > 255) return -1;
        int nv = DIGIT_TABLE[c1];
        return nv < radix ? nv : -1;
    }

    public int collectInt(final int radix) {
        boolean neg = false;
        int  r = 0;
        char c    ;
        if (current() == '-') {
            neg = true;
            next();
        }
        while ((c = current()) != DONE) {
            if (c == '_' || c == '\'') { next(); continue; }
            int nv = getDigit(c, radix);
            if (nv == -1) { break; }
            r *= radix;
            r += nv;
            next();
        }

        return neg ? -r : r;
    }

    public int collectInt() {
        return collectInt(10);
    }

    public long collectLong(final int radix) {
        boolean neg = false;
        long  r = 0;
        char c    ;
        if (current() == '-')
            neg = true;
        while ((c = current()) != DONE) {
            if (c == '_' || c == '\'') { next(); continue; }
            int nv = getDigit(c, radix);
            if (nv == -1) { break; }
            r *= radix;
            r += nv;
            next();
        }

        return neg ? -r : r;
    }

    public long collectLong() {
        return collectLong(10);
    }

    public float collectFloat() {
        boolean neg = false;
        if (current() == '-') {
            neg = true;
            next();
        }
        float f = collectInt(10);
        if (current() == '.') {
            next();
            char  c;
            float r = 0;
            float m = 0.1f;
            while ((c = current()) != DONE) {
                if (c == '_' || c == '\'') { next(); continue; }
                int nv = getDigit(c, 10);
                if (nv == -1) { break; }
                r += nv * m;
                m *= 0.1;
                next();
            }
            f += r;
        }
        return neg ? -f : f;
    }

    public double collectDouble() {
        boolean neg = false;
        if (current() == '-') {
            neg = true;
            next();
        }
        double f = collectLong(10);
        if (current() == '.') {
            next();
            char  c;
            double r = 0;
            double m = 0.1f;
            while ((c = current()) != DONE) {
                if (c == '_' || c == '\'') { next(); continue; }
                int nv = getDigit(c, 10);
                if (nv == -1) { break; }
                r += nv * m;
                m *= 0.1;
                next();
            }
            f += r;
        }
        return neg ? -f : f;
    }

}
