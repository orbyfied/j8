package net.orbyfied.j8.command.argument;

import net.orbyfied.j8.command.*;
import net.orbyfied.j8.command.argument.options.ArgumentOptions;
import net.orbyfied.j8.command.component.Completer;
import net.orbyfied.j8.command.component.Functional;
import net.orbyfied.j8.command.component.Primary;
import net.orbyfied.j8.command.exception.ErrorLocation;
import net.orbyfied.j8.command.exception.NodeParseException;
import net.orbyfied.j8.registry.Identifier;
import net.orbyfied.j8.util.StringReader;

import java.util.LinkedHashMap;
import java.util.Map;
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

    public Argument setOption(String id, Supplier<Object> supplier) {
        optionMap.put(id, supplier);
        return this;
    }

    public Argument setOption(String id, Object supplied) {
        optionMap.put(id, () -> supplied);
        return this;
    }

    public Argument setIdentifier(Identifier id) {
        this.identifier = id;
        return this;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Argument setType(ArgumentType<?> type) {
        this.type = type;
        return this;
    }

    public ArgumentType<?> getType() {
        return type;
    }

    private void putOptions(Context context) {
        for (Map.Entry<String, Supplier<Object>> entry : optionMap.entrySet())
            context.setLocalOption(entry.getKey(), entry.getValue().get());
    }

    private void remOptions(Context context) {
        for (String key : optionMap.keySet())
            context.unsetLocalOption(key);
    }

    @Override
    public void walked(Context ctx, StringReader reader) {
        // error location
        int startIndex = reader.index();
        // value
        Object v;

        // add options
        putOptions(ctx);

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

        // remove options
        remOptions(ctx);

        // set value
        ctx.setArgument(identifier, v);
    }

    @Override
    public void execute(Context ctx) { }

    @Override
    public boolean selects(Context ctx, StringReader reader) {
        putOptions(ctx);
        boolean b = type.accepts(ctx, reader);
        remOptions(ctx);
        return b;
    }

    @Override
    public int priority() {
        return options != null ? options.priority() : 0;
    }

    @Override
    public void complete(Context context, SuggestionAccumulator suggestions, StringReader reader) {
        putOptions(context);
        type.suggest(context, suggestions);
        remOptions(context);
    }

}
