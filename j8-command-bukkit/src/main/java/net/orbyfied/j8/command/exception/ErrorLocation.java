package net.orbyfied.j8.command.exception;

import net.md_5.bungee.api.ChatColor;
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
     * @see ErrorLocation#getLocationString(int, int)
     * Defaults both {@code prevStart} and {@code prevEnd}
     * to {@code 6}
     */
    public String getLocationString() {
        return getLocationString(6, 6);
    }

    /**
     * Create a nicely formatted error location string.
     * @param prevStart The preview length on the start.
     * @param prevEnd The preview length on the end.
     * @return The string.
     * TODO: change to components with j8-message api
     */
    public String getLocationString(int prevStart, int prevEnd) {
        // create builder
        StringBuilder b = new StringBuilder();

        // append prefix
        b.append(ChatColor.GREEN).append("...");

        // append index
        b.append(ChatColor.GRAY).append("[").append(getStartIndex()).append(":").append(getEndIndex()).append("]")
                .append(" ");

        // append substrings
        String str = reader.getString();

        // figure out locations
        int maxIdx = str.length() - 1;
        int startIndex0 = Math.max(Math.min(getStartIndex(), maxIdx), 0);
        int endIndex0   = Math.max(Math.min(getEndIndex(), maxIdx), 0);
        int startIndex  = Math.min(startIndex0, endIndex0);
        int endIndex    = Math.max(startIndex0, endIndex0);

        int pStartIndex = Math.max(Math.min(startIndex - prevStart, maxIdx), 0);
        int pEndIndex   = Math.max(Math.min(endIndex   + prevEnd,   maxIdx), 0);

        // get all string parts
        String subPrefix = str.substring(pStartIndex, startIndex);
        String sub       = str.substring(startIndex,  endIndex);
        String subSuffix = str.substring(endIndex,    pEndIndex);

        // append all parts formatted
        b.append(ChatColor.GREEN).append(subPrefix);
        b.append(ChatColor.RED).append(ChatColor.UNDERLINE).append(sub);
        b.append(ChatColor.GREEN).append(subSuffix);

        // append suffix
        b.append(ChatColor.GREEN).append("...");

        // return
        return b.toString();
    }

}
