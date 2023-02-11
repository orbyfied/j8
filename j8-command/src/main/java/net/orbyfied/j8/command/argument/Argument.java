package net.orbyfied.j8.command.argument;

import net.orbyfied.j8.command.AbstractNodeComponent;
import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.Node;
import net.orbyfied.j8.command.SuggestionAccumulator;
import net.orbyfied.j8.command.argument.options.ArgumentCompleter;
import net.orbyfied.j8.command.argument.options.ArgumentOptions;
import net.orbyfied.j8.command.component.Completer;
import net.orbyfied.j8.command.component.Functional;
import net.orbyfied.j8.command.component.Primary;
import net.orbyfied.j8.command.exception.ErrorLocation;
import net.orbyfied.j8.command.exception.NodeParseException;
import net.orbyfied.j8.registry.Identifier;
import net.orbyfied.j8.util.StringReader;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class Argument
        extends AbstractNodeComponent
        implements Functional, Primary, Completer {

    // the argument ID
    protected Identifier identifier;

    // the argument type
    protected ArgumentType<?> type;
    // the argument options
    protected ArgumentOptions options;

    // the options - TODO: replace with ArgumentOptions object
    protected LinkedHashMap<String, Supplier<Object>> optionMap = new LinkedHashMap<>();

    public Argument(Node node) {
        super(node);
        Node parent = node;
        while ((parent = parent.parent()).hasComponentOf(Argument.class)) { }
        identifier = new Identifier(null, node.getName());
    }

    public ArgumentOptions getOptions() {
        return options;
    }

    public Argument setOptions(ArgumentOptions options) {
        this.options = options;
        return this;
    }

    public Argument setIdentifier(Identifier id) {
        this.identifier = id;
        return this;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    @SuppressWarnings("unchecked")
    public Argument setType(ArgumentType<?> type) {
        this.type = (ArgumentType<?>) type;
        return this;
    }

    public ArgumentType<?> getType() {
        return type;
    }

    @Override
    public void walked(Context ctx, StringReader reader) {
        // error location
        int startIndex = reader.index();
        // value
        Object v;

        try {
            // parse value
            v = type.parse(ctx, reader);
        } catch (Exception e) {

            // don't create error chain
            if (e instanceof NodeParseException) {
                throw e;
            }

            // throw error
            int endIndex = reader.index();
            throw new NodeParseException(
                    node.root(),
                    node,
                    new ErrorLocation(reader, startIndex, endIndex),
                    e
            );

        }

        // set value
        ctx.setArgument(identifier, v);
    }

    @Override
    public void execute(Context ctx) { }

    @Override
    public boolean selects(Context ctx, StringReader reader) {
        return type.accepts(ctx, reader);
    }

    @Override
    public int priority() {
        return options != null ? options.priority() : 0;
    }

    @Override
    public void complete(Context context, SuggestionAccumulator suggestions, StringReader reader) {
        type.suggest(context, suggestions);
        if (options != null) {
            for (ArgumentCompleter completer : options.completers()) {
                completer.complete(this, context, suggestions);
            }
        }
    }

}
