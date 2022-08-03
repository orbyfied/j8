package net.orbyfied.j8.command.component;

import net.orbyfied.j8.command.AbstractNodeComponent;
import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.Node;
import net.orbyfied.j8.command.SuggestionAccumulator;
import net.orbyfied.j8.command.impl.CommandNodeExecutor;
import net.orbyfied.j8.util.StringReader;

import java.util.Set;

/**
 * A node component which executes code
 * when a node is walked or executed.
 */
public class Executable
        extends AbstractNodeComponent
        implements Selecting, Functional, Completable {

    public Executable(Node node) {
        super(node);
    }

    /**
     * The node executor for the event
     * that this node is walked over when
     * executing OR when suggesting.
     */
    private CommandNodeExecutor walkExecutor;

    /**
     * The node executor for the event
     * that this node was the last executable
     * and was chosen to be executed. This
     * only happens when executing, not when
     * suggesting.
     */
    private CommandNodeExecutor executor;

    /**
     * A quick cache for all aliases this node has.
     * For fast "contains(Object)" calls.
     */
    private Set<String> aliases = Set.of(node.getAliases().toArray(new String[0]));

    /* Getters and Setters */

    public Executable setWalkExecutor(CommandNodeExecutor e) {
        this.walkExecutor = e;
        return this;
    }

    public Executable setExecutor(CommandNodeExecutor e) {
        this.executor = e;
        return this;
    }

    /* Selecting, Functional and Completable */

    @Override
    public boolean selects(Context ctx, StringReader reader) {
        // collect the label used
        String s = reader.collect(c -> c != ' ');
        // check against the name
        if (s.equals(node.getName()))
            return true;
        // check if its an alias
        return aliases.contains(s);
    }

    @Override
    public void walked(Context ctx, StringReader reader) {
        // skip over the label
        // this is important because at this
        // stage we are still parsing, meaning
        // its our responsibility to make sure
        // the parser isnt fucked in the ass
        reader.collect(c -> c != ' ');
        // invoke walk executor if present
        if (walkExecutor != null)
            walkExecutor.execute(ctx, node);
    }

    @Override
    public void execute(Context ctx) {
        // invoke executor if present
        if (executor != null)
            executor.execute(ctx, node);
    }

    @Override
    public void completeSelf(Context context, Node from, SuggestionAccumulator suggestions) {
        // suggest node name
        suggestions.suggest(node.getName());
    }

}
