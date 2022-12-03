package net.orbyfied.j8.util;

import java.util.*;

public class StringUtil {

    /* ------- Format & Patterns -------- */

    /**
     * A placeholder part of a format pattern.
     */
    static record PlacePart(int index, char type, Object[] data) { }

    /**
     * Formats one part to a string.
     * @param part The part.
     * @param values Values.
     * @return The string or null if it failed.
     */
    private static String formatPart(Object part, Object... values) {
        // check null
        if (part == null)
            // return null because part was null
            return null;

        // check for part type
        // placeholder in this case
        if (part instanceof PlacePart pp) {
            // check placeholder type
            switch (pp.type) {
                // generic, just convert to string
                case 0 -> {
                    if (pp.index >= values.length)
                        return "null";
                    return Objects.toString(values[pp.index]);
                }
            }

            // unknown type
            return null;
        } else {
            // just to string part
            return Objects.toString(part);
        }
    }

    /**
     * Formatting pattern.
     */
    public static class FormatPattern {

        /**
         * The parts.
         */
        final List<Object> parts = new ArrayList<>();

        /**
         * Get a part at index I.
         * @param i The index.
         * @return The part or null if outbound.
         */
        public Object part(int i) {
            if (i < 0 || i >= parts.size())
                return null;
            return parts.get(i);
        }

        /**
         * To strings one part of this pattern.
         * @param i The part index.
         * @param values The values.
         * @return The string or null if something failed.
         */
        public String string(int i, Object... values) {
            Object part = parts.get(i);
            return formatPart(part, values);
        }

        /**
         * Formats this pattern using the values
         * and returns it as a string.
         * @param values Values.
         * @return The string or null if it failed.
         */
        public String format(Object... values) {
            // setup
            StringBuilder b = new StringBuilder();

            // for every part
            int l = parts.size();
            for (int i = 0; i < l; i++) {
                // get part
                Object part = parts.get(i);

                // append formatted part
                String fmt = formatPart(part, values);
                if (fmt != null)
                    b.append(fmt);
            }

            // return string
            return b.toString();
        }

    }

    // parses a placeholder
    private static PlacePart parsePlaceholder(StringReader reader) {
        // parse index number
        int index = reader.collectInt(10);

        // TODO: parse types and data
        List<Object> data = new ArrayList<>();
        char         type = '\0';

        // skip end char
        if (reader.current() != '}')
            throw new IllegalArgumentException("expected '}' to close pattern placeholder");
        reader.next();

        // create place part
        return new PlacePart(index, type, data.toArray());
    }

    /**
     * Compiles the format into a pattern and
     * immediately formats it with the values.
     * @param format The format.
     * @param values Values.
     * @return The formatted string.
     */
    public static String format(String format, Object... values) {
        return pattern(format).format(values);
    }

    /**
     * Compiles a format pattern from a string.
     * @param str The string to parse.
     * @return The pattern.
     */
    public static FormatPattern pattern(String str) {
        // create empty pattern
        FormatPattern pattern = new FormatPattern();

        // parse string
        StringBuilder buf = new StringBuilder();
        StringReader reader = new StringReader(str);
        char c;
        while ((c = reader.current()) != StringReader.DONE) {
            // check for escape
            if (c == '\\') {
                buf.append(reader.next());
                reader.next();
                continue;
            }

            // check for placeholder
            if (c == '{') {
                // append and reset buffer
                pattern.parts.add(buf);
                buf = new StringBuilder();

                // advance and parse placeholder
                reader.next();
                PlacePart part = parsePlaceholder(reader);
                pattern.parts.add(part);

                // continue without advancing, because the
                // parsing of the placeholder already did
                // this for us
                continue;
            }

            // append character to buffer and advance
            buf.append(c);
            reader.next();
        }

        // append final buffer
        pattern.parts.add(buf);

        // return pattern
        return pattern;
    }

    /* -------- Other --------- */

    public static String toStringDebug(Object o) {
        if (o == null) return "<null>";
        if (o instanceof CharSequence) return "\"" + o + "\"";
        if (o instanceof Character) return "'" + o + "'";
        if (o.getClass().isArray()) {
            Object[] arr = (Object[]) o;
            StringJoiner j = new StringJoiner(",", "[ ", " ]");
            for (Object e : arr)
                j.add(toStringDebug(e));
            return j.toString();
        }
        if (o instanceof Map<?, ?> map) {
            StringJoiner j = new StringJoiner(",", "{ ", " }");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                j.add(toStringDebug(entry.getKey()) + " : " + toStringDebug(entry.getValue()));
            }
        }
        return o.toString();
    }

    public static String extendTail(String str, int targetLength, char ext) {
        int t = targetLength - str.length();
        if (t > 0)
            str += ("" + ext).repeat(t);
        return str;
    }

}
