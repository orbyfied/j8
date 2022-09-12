package net.orbyfied.j8.util.math.expr;

import net.orbyfied.j8.util.StringReader;

public class StringLocation {

//    public static final StringLocation EMPTY = new StringLocation("<unkown>", "", -1, -1);
    public static final StringLocation EMPTY = null;

    public static StringLocation cover(StringLocation s, StringLocation e) {
        if (s == null || e == null)
            return null;
        return new StringLocation(
                s.fn, s.str,
                Math.min(s.startIndex, e.startIndex),
                Math.max(s.endIndex, e.endIndex)
        );
    }

    ///////////////////////////////

    String fn;
    String str;
    int startIndex;
    int endIndex;

    public StringLocation(String fn, String str, int startIndex, int endIndex) {
        this.str = str;
        this.fn  = fn;
        this.startIndex = startIndex;
        this.endIndex   = endIndex;
    }

    public StringLocation(String fn, StringReader str, int startIndex, int endIndex) {
        this.str = str.getString();
        this.fn  = fn;
        this.startIndex = startIndex;
        this.endIndex   = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public String getFilename() {
        return fn;
    }

    public String getString() {
        return str;
    }

    ////////////////////////////////

    @Override
    public String toString() {
        return "(" + startIndex + ":" + endIndex + ") in " + fn;
    }

    public String toStringFancy() {
        return toString();
    }

}
