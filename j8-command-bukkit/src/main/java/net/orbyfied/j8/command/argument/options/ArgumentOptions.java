package net.orbyfied.j8.command.argument.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgumentOptions {

    // the custom completers
    protected List<ArgumentCompleter<?>> completers = new ArrayList<>();

    // the argument priority
    protected int priority = 0;

    public ArgumentOptions withCompleter(ArgumentCompleter<?> completer) {
        this.completers.add(completer);
        return this;
    }

    public ArgumentOptions completers(List<ArgumentCompleter<?>> completers) {
        this.completers = completers;
        return this;
    }

    public ArgumentOptions completers(ArgumentCompleter<?>... completers) {
        return completers(Arrays.asList(completers));
    }

    public List<ArgumentCompleter<?>> completers() {
        return completers;
    }

    public ArgumentOptions priority(int priority) {
        this.priority = priority;
        return this;
    }

    public int priority() {
        return priority;
    }

}
