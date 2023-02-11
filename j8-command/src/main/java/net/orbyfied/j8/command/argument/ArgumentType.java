package net.orbyfied.j8.command.argument;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.SuggestionAccumulator;
import net.orbyfied.j8.util.StringReader;

/**
 * The type of an argument.
 *
 * @param <T> The Java runtime type.
 */
public interface ArgumentType<T> {

    /**
     * The un-parameterized identifier.
     *
     * @return The identifier.
     */
    TypeIdentifier getBaseIdentifier();

    /**
     * The full identifier.
     *
     * @return The identifier.
     */
    default TypeIdentifier getIdentifier() {
        return getBaseIdentifier();
    }

    /**
     * Get the Java runtime value type.
     */
    Class<?> getType();

    /* Functionality */

    boolean accepts(Context context, StringReader reader);

    T parse(Context context, StringReader reader);

    void write(Context context, StringBuilder builder, T v);

    void suggest(Context context, SuggestionAccumulator suggestions);

}
