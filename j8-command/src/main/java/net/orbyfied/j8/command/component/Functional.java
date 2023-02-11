package net.orbyfied.j8.command.component;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.NodeComponent;
import net.orbyfied.j8.util.StringReader;

/**
 * Designates a functional component of
 * a command node, i.e. a component which responds
 * to events regarding parsing and execution.
 */
public interface Functional extends NodeComponent {

    /**
     * Called when the node this component
     * is on is walked over by the tree search.
     *
     * This is during the parsing stage.
     *
     * @param ctx The command context.
     * @param reader The string reader.
     */
    void walked(Context ctx, StringReader reader);

    /**
     * Called when the node this component
     * is on is evaluated for execution.
     *
     * This is after the parsing stage.
     *
     * @param ctx The command context.
     */
    void execute(Context ctx);

}
