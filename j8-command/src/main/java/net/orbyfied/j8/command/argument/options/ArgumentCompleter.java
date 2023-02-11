package net.orbyfied.j8.command.argument.options;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.SuggestionAccumulator;
import net.orbyfied.j8.command.argument.Argument;

/**
 * Argument completer function.
 */
@FunctionalInterface
public interface ArgumentCompleter {

    void complete(Argument argument, Context context, SuggestionAccumulator accumulator);

}
