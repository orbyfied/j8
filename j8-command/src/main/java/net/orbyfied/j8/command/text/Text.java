package net.orbyfied.j8.command.text;

import net.orbyfied.j8.util.StringReader;

import java.awt.*;
import java.util.function.Function;

/**
 * Utilities for working with formatted text
 * independent of platform.
 */
public class Text {

    public static String literal(String in) {
        return new StringReader(in).escapeRemaining(c -> c == '&', c -> "\\" + c);
    }

    /**
     * Translates the format specifiers in the
     * text into the appropriate formatting codes
     * using the given translator.
     *
     * @param translator The translator function.
     *                   If it is null all formats will be stripped from the string.
     * @param input The input string.
     * @return The translated string.
     */
    public static String translate(Function<String, Object> translator,
                                   String input) {
        StringBuilder builder = new StringBuilder(input.length());
        StringReader reader = new StringReader(input);
        char c;
        while ((c = reader.current()) != StringReader.DONE) {
            // literal char
            if (c == '\\') {
                builder.append(reader.next());
                reader.next();
                continue;
            }

            // format specifier
            if (c == '&') {
                String str = reader.collect(c1 -> c1 != ';');
                reader.next();
                if (translator != null)
                    builder.append(translator.apply(str));
                continue;
            }

            // character
            builder.append(c);
            reader.next();
        }

        return builder.toString();
    }

    /* Format Specifiers */

    public static String spec(String str) {
        return "&" + str + ";";
    }

    public static String hex(String str) {
        return spec("#" + str);
    }

    public static String hex(Color color) {
        return null; // TODO
    }

    // constants //
    public static final String BLACK        = spec("0");
    public static final String DARK_BLUE    = spec("1");
    public static final String DARK_GREEN   = spec("2");
    public static final String DARK_AQUA    = spec("3");
    public static final String DARK_RED     = spec("4");
    public static final String DARK_PURPLE  = spec("5");
    public static final String GOLD         = spec("6");
    public static final String GRAY         = spec("7");
    public static final String DARK_GRAY    = spec("8");
    public static final String BLUE         = spec("9");
    public static final String GREEN        = spec("a");
    public static final String AQUA         = spec("b");
    public static final String RED          = spec("c");
    public static final String LIGHT_PURPLE = spec("d");
    public static final String YELLOW       = spec("e");
    public static final String WHITE        = spec("f");

    public static final String RESET         = spec("r");
    public static final String BOLD          = spec("l");
    public static final String OBFUSCATED    = spec("k");
    public static final String STRIKETHROUGH = spec("m");
    public static final String UNDERLINE     = spec("n");
    public static final String ITALIC        = spec("o");

}
