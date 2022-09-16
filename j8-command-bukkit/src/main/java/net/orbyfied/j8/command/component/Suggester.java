package net.orbyfied.j8.command.component;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.NodeComponent;
import net.orbyfied.j8.command.SuggestionAccumulator;
import net.orbyfied.j8.util.StringReader;

/**
 * A node component which handles completion
 * of the current or next node.
 */
public interface Suggester extends NodeComponent {

    /**
     * Should suggest possibilities for the
     * following node.
     * @param ctx The context.
     * @param builder The suggestions builder.
     * @param reader The string reader.
     */
    void suggest(Context ctx,
                 SuggestionAccumulator builder,
                 StringReader reader);

}
