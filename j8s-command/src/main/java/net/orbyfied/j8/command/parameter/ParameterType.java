package net.orbyfied.j8.command.parameter;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.SuggestionAccumulator;
import net.orbyfied.j8.util.StringReader;

public interface ParameterType<T> {

    TypeIdentifier getBaseIdentifier();

    default TypeIdentifier getIdentifier() {
        return getBaseIdentifier();
    }

    Class<?> getType();

    boolean accepts(Context context, StringReader reader);

    T parse(Context context, StringReader reader);

    void write(Context context, StringBuilder builder, T v);

    void suggest(Context context, SuggestionAccumulator suggestions);

}
