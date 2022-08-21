package net.orbyfied.j8.command.component;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.NodeComponent;
import net.orbyfied.j8.command.SuggestionAccumulator;
import net.orbyfied.j8.util.StringReader;

/**
 * A node component which handles completion
 * for the node it's been assigned to.
 */
public interface Completer extends NodeComponent {

    /**
     * Should complete the current node string.
     * @param context The context.
     * @param suggestions The suggestion builder.
     * @param reader The node to complete.
     */
    void complete(Context context,
                  SuggestionAccumulator suggestions,
                  StringReader reader);

}
