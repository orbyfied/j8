package net.orbyfied.j8.command.util;

import net.orbyfied.j8.util.StringReader;
import org.bukkit.ChatColor;

public class StringLocation implements StringLocatable<Void> {

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
    public int startIndex;
    public int endIndex;
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

    public String toStringFancy(final int off, boolean f) {
        StringBuilder b = new StringBuilder();
        b.append(ChatColor.RED).append("(").append(startIndex).append(":").append(endIndex).append(") in ")
                .append(fn).append(ChatColor.RESET);

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
                .append(ChatColor.RESET + "" + ChatColor.DARK_GRAY).append("...")
                .append(ChatColor.RESET + "" + ChatColor.GRAY).append(str.substring(ss, se))
                .append(ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.UNDERLINE).append(str.substring(bs, be))
                .append(ChatColor.RESET + "" + ChatColor.GRAY).append(str.substring(es, ee))
                .append(ChatColor.RESET + "" + ChatColor.DARK_GRAY).append("...");
        return b.toString();
    }

    @Override
    public Void located(StringLocation loc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void located(StringLocatable<?> loc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringLocation getLocation() {
        return this;
    }

}
