package net.orbyfied.j8.command.exception;

import net.orbyfied.j8.command.text.Text;
import net.orbyfied.j8.util.StringReader;

/**
 * The location of an error in
 * a string.
 */
public class ErrorLocation {

    /**
     * The string (reader).
     */
    protected StringReader reader;

    /**
     * The start index.
     */
    protected int fromIndex;

    /**
     * The end index.
     */
    protected int toIndex;

    /**
     * Constructor.
     * @param reader The string reader.
     * @param fromIndex The start of the problematic segment.
     * @param toIndex The end of the problematic segment.
     */
    public ErrorLocation(StringReader reader,
                         int fromIndex,
                         int toIndex) {
        // set reader
        this.reader = reader;

        // swap indices if necessary
        if (fromIndex > toIndex) {
            fromIndex = toIndex;
            toIndex   = fromIndex;
        }

        // set safe
        this.fromIndex = Math.min(reader.getString().length() - 1, fromIndex);
        this.toIndex   = Math.max(reader.getString().length() - 1, toIndex);
    }

    /* Getters. */

    public int getStartIndex() {
        return fromIndex;
    }

    public int getEndIndex() {
        return toIndex;
    }

    public StringReader getReader() {
        return reader;
    }

    /**
     * @see ErrorLocation#getLocationString(int)
     * Defaults {@code off} to {@code 10}
     */
    public String getLocationString() {
        return getLocationString(10);
    }

    /**
     * Create a nicely formatted error location string.
     * @return The string.
     * TODO: change to components with j8-message api
     */
    public String getLocationString(int off) {
        String str = reader.getString();

        StringBuilder b = new StringBuilder();
        b.append(Text.RED).append("(").append(fromIndex).append(":").append(toIndex).append(")")
                .append(Text.RESET);

        if (toIndex >= str.length())
            str = str + " ";

        final int l = str.length();

        int ss = Math.max(0, Math.min(l, fromIndex - off));
        int se = Math.max(0, Math.min(l, fromIndex));
        int bs = Math.max(0, Math.min(l, fromIndex));
        int be = Math.max(0, Math.min(l, toIndex + 1));
        int es = Math.max(0, Math.min(l, toIndex + 1));
        int ee = Math.max(0, Math.min(l, toIndex + off));

        b
                .append(Text.RESET + "" + Text.DARK_GRAY).append("...")
                .append(Text.RESET + "" + Text.GRAY).append(str.substring(ss, se))
                .append(Text.RESET + "" + Text.RED + "" + Text.UNDERLINE).append(str.substring(bs, be))
                .append(Text.RESET + "" + Text.GRAY).append(str.substring(es, ee))
                .append(Text.RESET + "" + Text.DARK_GRAY).append("...");
        return b.toString();
    }

}
