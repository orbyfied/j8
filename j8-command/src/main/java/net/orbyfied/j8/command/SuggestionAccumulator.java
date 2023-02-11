package net.orbyfied.j8.command;

import java.util.Stack;
import java.util.function.BiPredicate;

/**
 * The suggestion list builder.
 *
 * Consumes the given options, transforms them
 * and provides them back to the user as
 * suggestions.
 */
public abstract class SuggestionAccumulator {

    // the context
    Context context;

    public SuggestionAccumulator withContext(Context context) {
        this.context = context;
        return this;
    }

    // modifier stacks
    final Stack<String> prefixStack = new Stack<>();
    final Stack<String> suffixStack = new Stack<>();
    // filter stack
    final Stack<BiPredicate<Context, String>> filterStack = new Stack<>();

    public SuggestionAccumulator suggest(Object o) {
        if (o == null)
            return this;
        StringBuilder str = new StringBuilder(o.toString());

        // apply prefix and suffix
        for (String prefix : prefixStack)
            str.insert(0, prefix);
        for (String suffix : suffixStack)
            str.append(suffix);

        // filter
        String s = str.toString();
        for (BiPredicate<Context, String> filter : filterStack)
            if (!filter.test(context, s))
                return this;

        // suggest
        suggest0(str.toString());

        // return
        return this;
    }

    protected abstract void suggest0(String s);

    protected abstract void unsuggest0(String s);

    /* ---- Data ---- */

    public SuggestionAccumulator pushFilter(BiPredicate<Context, String> predicate) {
        filterStack.push(predicate);
        return this;
    }

    public SuggestionAccumulator popFilter() {
        filterStack.pop();
        return this;
    }

    public SuggestionAccumulator pushPrefix(String s) {
        prefixStack.push(s);
        return this;
    }

    public SuggestionAccumulator popPrefix() {
        prefixStack.pop();
        return this;
    }

    public SuggestionAccumulator pushSuffix(String s) {
        suffixStack.push(s);
        return this;
    }

    public SuggestionAccumulator popSuffix() {
        suffixStack.pop();
        return this;
    }

}
