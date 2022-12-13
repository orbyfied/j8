package net.orbyfied.j8.command.argument.options;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.SuggestionAccumulator;
import net.orbyfied.j8.command.argument.Argument;

public interface ArgumentCompleter<O extends ArgumentOptions> {

    void complete(Argument argument, Context context, O options,
                  SuggestionAccumulator accumulator);

}
