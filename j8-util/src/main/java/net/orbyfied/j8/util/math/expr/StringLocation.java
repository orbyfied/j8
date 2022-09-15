package net.orbyfied.j8.util.math.expr;

import net.orbyfied.j8.util.StringReader;

import java.math.BigDecimal;

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
    int ln = -1;

    public StringLocation(StringLocation loc, int startIndex, int endIndex) {
        this.fn  = loc.fn;
        this.str = loc.str;
        this.startIndex = startIndex;
        this.endIndex   = endIndex;
    }

    public StringLocation(String fn, String str, int startIndex, int endIndex) {
        this.str = str;
        this.fn  = fn;
        this.startIndex = startIndex;
        this.endIndex   = endIndex;
    }

    public StringLocation atLine(int ln) {
        this.ln = ln;
        return this;
    }

    public int getLn() {
        return ln;
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
        return "(" + startIndex + ":" + endIndex +
                (ln == -1 ? "" : " ln " + ln) + ") in " + fn;
    }

    private String ac(String c, boolean fmt) {
        if (!fmt)
            return "";
        return "\u001B[" + c + "m";
    }

    public String toStringFancy(final int off, boolean f) {
        StringBuilder b = new StringBuilder();
        b.append(ac("31", f)).append("(").append(startIndex).append(":").append(endIndex).append(") in ")
                .append(fn).append(ac("0", f));

        if (endIndex >= str.length())
            str = str + " ";

        final int l = str.length();

        int ss = Math.max(0, Math.min(l, startIndex - off));
        int se = Math.max(0, Math.min(l, startIndex));
        int bs = Math.max(0, Math.min(l, startIndex));
        int be = Math.max(0, Math.min(l, endIndex + 1));
        int es = Math.max(0, Math.min(l, endIndex + 1));
        int ee = Math.max(0, Math.min(l, endIndex + off));

        b
                .append(ac("0", f) + ac("90", f)).append("...")
                .append(ac("0", f) + ac("37", f)).append(str.substring(ss, se))
                .append(ac("0", f) + ac("31", f) + ac("4", f)).append(str.substring(bs, be))
                .append(ac("0", f) + ac("37", f)).append(str.substring(es, ee))
                .append(ac("0", f) + ac("90", f)).append("...");
        return b.toString();
    }

}
