package net.orbyfied.j8.command.component;

import net.orbyfied.j8.command.*;
import net.orbyfied.j8.util.StringReader;

/**
 * A node component which handles completion
 * of the current or next node.
 */
public abstract class Suggester extends AbstractNodeComponent {

    /**
     * Create default suggester.
     * @param node The node.
     * @return The suggester.
     */
    public static Suggester defaults(final Node node) {
        return new Suggester(node) {
            @Override
            public void suggest(Context ctx, SuggestionAccumulator builder, StringReader reader) {
                // get partial
                String partial = reader.branch().collect();
                builder.pushFilter((context, s) -> s.contains(partial));

                // for each child node
                for (Node child : node.getChildren()) {
                    // complete
                    Completer completer = child.getComponentOf(Completer.class);
                    if (completer != null) {
                        completer.complete(ctx, builder, reader);
                    }
                }

                builder.popFilter();
            }
        };
    }

    ///////////////////////////////////////////////////////

    public Suggester(Node node) {
        super(node);
    }

    /**
     * Should suggest possibilities for the
     * following node.
     * @param ctx The context.
     * @param builder The suggestions builder.
     * @param reader The string reader.
     */
    public abstract void suggest(Context ctx,
                                 SuggestionAccumulator builder,
                                 StringReader reader);

}
