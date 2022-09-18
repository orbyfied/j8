package net.orbyfied.j8.registry;

import net.orbyfied.j8.util.Reader;
import net.orbyfied.j8.util.StringReader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A key.
 * Formatted {@code a/b/c} or {@code a.b.c}
 * Can be checked against another key format,
 * like {@code *->clientbound} will
 */
public class Key {

    public static Key constant(StringReader b) {
        return null; // TODO
    }

    public static Key constant(String str) {
        return constant(new StringReader(str));
    }

    ///////////////////////////////////////

    /**
     * A key part.
     */
    public static abstract class Part {

        public abstract boolean equals(Part b);

        public abstract int check(Key a, Key b,
                                  Reader<Part> aParts, Reader<Part> bParts);

    }

    //////////////////////////////////////

    // this key as string
    String asString;
    // the key parts
    final List<Part> parts = new ArrayList<>();

    /**
     * Get a part by index. Will
     * return null if the index is outbound.
     * @param i The index.
     * @return The part or null if outbound.
     */
    public Part get(int i) {
        if (i < 0 || i >= parts.size())
            return null;
        return parts.get(i);
    }

    public boolean matches(Key format) {
        // TODO
        return false;
    }

    public String asString() {
        // check cached
        if (asString != null)
            return asString;

        // create string
        return asString = createString();
    }

    public String createString() {
        // TODO
        return "<created string: todo>";
    }

}
