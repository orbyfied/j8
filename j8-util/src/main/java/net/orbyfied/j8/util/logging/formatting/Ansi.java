package net.orbyfied.j8.util.logging.formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class for ANSI information.
 * Special thanks to: https://github.com/dialex/JColor for showing me how to do RGB codes.
 */
public class Ansi {

    /** The escape character which denotes the ANSI code. */
    public static final char ESC_CHAR = 27;

    /** The separator between ANSI attributes. */
    public static final String SEPARATOR = ";";

    /** Every ANSI attribute/sequence starts like this. */
    public static final String PREFIX = ESC_CHAR + "[";

    /** Every ANSI attribute/sequence ends like this. */
    public static final String SUFFIX = "m";

    /**
     * Encodes an array of attributes into a valid ANSI sequence.
     * @param attributes The specified of the attributes.
     * @return The encoded sequence.
     */
    public static String encode(AnsiAttr... attributes) {
        // create string builder with prefix
        StringBuilder builder = new StringBuilder(PREFIX);

        // loop over attributes and append them
        for (AnsiAttr attr : attributes) {
            // check for null
            if (attr == null) continue;

            // get and check code
            String s = attr.code();
            if (s.equals("")) continue;

            // append code and separator
            builder.append(s).append(SEPARATOR);
        }

        // remove trailing separator and append suffix
        builder.delete(builder.length() - SEPARATOR.length() - 1, builder.length()).append(SUFFIX);

        // return
        return builder.toString();
    }

    /**
     * Parses a string with sequences prefixed by 'prefix' (+'hexprefix' if RGB)
     * to a string with ANSI attributes.
     * @param s The string to parse.
     * @param prefix The prefix of all of the sequences.
     * @param hexprefix The prefix of all of the RGB (hexadecimal) sequences.
     *                  The final prefix is <code>prefix + hexprefix</code>
     * @return The parsed string.
     */
    public static String translate(String s, char prefix, char hexprefix) {
        // TODO: implement lol
        return null;
    }

    /** https://stackoverflow.com/a/14693789/148377040 */
    private static final String  strip_regex = "\\x1B(?:[@-Z\\-_]|\\[[0-?]*[ -/]*[@-~])";
    private static final Pattern strip_patrn = Pattern.compile(strip_regex);

    /**
     * Strips the string 's' of all of its
     * ANSI sequences.
     * @param s The string.
     * @return The stripped string.
     */
    public static String strip(String s) {
        // create matcher
        Matcher matcher = strip_patrn.matcher(s);

        // replace all
        return matcher.replaceAll("");
    }

}
