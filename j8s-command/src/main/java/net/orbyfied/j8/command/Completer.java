package net.orbyfied.j8.command;

public interface Completer extends NodeComponent {

    void completeSelf(Context context,
                      Node from,
                      SuggestionAccumulator suggestions);

}
