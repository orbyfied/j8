package net.orbyfied.j8.command.util;

public interface StringLocatable<S> {

    S located(StringLocation loc);

    @SuppressWarnings("unchecked")
    default S located(StringLocatable<?> loc) {
        if (loc != null)
            return located((StringLocation)loc.getLocation());
        return (S) this;
    }

    StringLocation getLocation();

}
