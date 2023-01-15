package net.orbyfied.j8.command.exception;

import net.orbyfied.j8.util.StringReader;
import net.md_5.bungee.api.ChatColor;

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
        b.append(net.md_5.bungee.api.ChatColor.RED).append("(").append(fromIndex).append(":").append(toIndex).append(")")
                .append(net.md_5.bungee.api.ChatColor.RESET);

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
                .append(net.md_5.bungee.api.ChatColor.RESET + "" + net.md_5.bungee.api.ChatColor.DARK_GRAY).append("...")
                .append(net.md_5.bungee.api.ChatColor.RESET + "" + net.md_5.bungee.api.ChatColor.GRAY).append(str.substring(ss, se))
                .append(net.md_5.bungee.api.ChatColor.RESET + "" + net.md_5.bungee.api.ChatColor.RED + "" + net.md_5.bungee.api.ChatColor.UNDERLINE).append(str.substring(bs, be))
                .append(net.md_5.bungee.api.ChatColor.RESET + "" + net.md_5.bungee.api.ChatColor.GRAY).append(str.substring(es, ee))
                .append(net.md_5.bungee.api.ChatColor.RESET + "" + ChatColor.DARK_GRAY).append("...");
        return b.toString();
    }

}
