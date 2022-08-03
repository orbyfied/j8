package net.orbyfied.j8.command.component;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.Node;
import net.orbyfied.j8.command.NodeComponent;
import net.orbyfied.j8.command.SuggestionAccumulator;

/**
 * A node component which handles completion
 * for the node it's been assigned to.
 */
public interface Completable extends NodeComponent {

    /**
     * Should complete the current node string.
     * @param context The context.
     * @param from The node to complete.
     * @param suggestions The suggestion builder.
     */
    void completeSelf(Context context,
                      Node from,
                      SuggestionAccumulator suggestions);

}
