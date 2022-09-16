package net.orbyfied.j8.command;

import java.util.Stack;

public abstract class SuggestionAccumulator {

    // modifier stacks
    final Stack<String> prefixStack = new Stack<>();
    final Stack<String> suffixStack = new Stack<>();

    public SuggestionAccumulator suggest(Object o) {
        if (o == null)
            return this;
        StringBuilder str = new StringBuilder(o.toString());

        // apply prefix and suffix
        for (String prefix : prefixStack)
            str.insert(0, prefix);
        for (String suffix : suffixStack)
            str.append(suffix);

        // suggest
        suggest0(str.toString());

        // return
        return this;
    }

    protected abstract void suggest0(String s);

    protected abstract void unsuggest0(String s);

    /* ---- Data ---- */

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
