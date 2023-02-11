package net.orbyfied.j8.command.component;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.NodeComponent;
import net.orbyfied.j8.util.StringReader;

/**
 * Designates the primary component
 * of a command node.
 */
public interface Primary extends NodeComponent {

    /**
     * Check if the node this component is on
     * can be qualified for selection.
     *
     * @param ctx The command context.
     * @param reader The string reader.
     * @return True/false.
     */
    boolean selects(Context ctx, StringReader reader);

    /**
     * The priority the node this component
     * is on has in the comparison for selection.
     *
     * @return The priority.
     */
    int priority();

}
