package net.orbyfied.j8.command;

public interface SuggestionAccumulator {

    SuggestionAccumulator suggest(Object o);

    SuggestionAccumulator unsuggest(Object o);

}
