package net.orbyfied.j8.command.impl;

import net.orbyfied.j8.command.*;
import net.orbyfied.j8.command.component.Completable;
import net.orbyfied.j8.command.component.Suggester;
import net.orbyfied.j8.util.StringReader;

public class DefaultSuggester
        extends AbstractNodeComponent
        implements Suggester {

    public DefaultSuggester(Node node) {
        super(node);
    }

    @Override
    public void suggestNext(Context ctx,
                            SuggestionAccumulator builder,
                            StringReader reader,
                            Node next) {
        if (next == null) {
            for (Node c : node.getChildren()) {
                Completable comp = c.getComponentOf(Completable.class);
                if (comp != null)
                    comp.completeSelf(ctx, node, builder);
            }
        } else {
            Completable comp = next.getComponentOf(Completable.class);
            if (comp != null)
                comp.completeSelf(ctx, next, builder);
        }
    }

}
