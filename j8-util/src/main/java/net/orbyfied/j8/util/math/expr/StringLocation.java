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

    public String toStringFancy(boolean format) {
        StringBuilder b = new StringBuilder();
        b.append("\u001B[31m (").append(startIndex).append(":").append(endIndex).append(") in ")
                .append(fn).append("\u001B[0m");

        final int off = 6;
        final int l   = str.length();

        int ss = Math.max(0, Math.min(l, startIndex - off));
        int se = Math.max(0, Math.min(l, startIndex));
        int bs = Math.max(0, Math.min(l, startIndex));
        int be = Math.max(0, Math.min(l, endIndex)) + 1;
        int es = Math.max(0, Math.min(l, endIndex + 1));
        int ee = Math.max(0, Math.min(l, endIndex + off));

        b
                .append(" \u001B[0m\u001B[90m...")
                .append("\u001B[0m\u001B[37m").append(str.substring(ss, se))
                .append("\u001B[0m\u001B[31m\u001B[4m").append(str.substring(bs, be))
                .append("\u001B[0m\u001B[37m").append(str.substring(es, ee))
                .append("\u001B[0m\u001B[90m...");
        return b.toString();
    }

}
