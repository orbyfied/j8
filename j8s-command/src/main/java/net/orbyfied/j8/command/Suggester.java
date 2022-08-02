package net.orbyfied.j8.command;

import net.orbyfied.j8.util.StringReader;

public interface Suggester extends NodeComponent {

    void suggestNext(Context ctx,
                     SuggestionAccumulator builder,
                     StringReader reader,
                     Node next);

}
