package net.orbyfied.j8.expr.parser;

import net.orbyfied.j8.expr.StringLocation;
import net.orbyfied.j8.util.StringReader;

public class Token<T> {

    // the location
    public StringLocation loc;

    // the type of this token
    final TokenType type;
    // the token value
    T value;

    public Token(
            TokenType type
    ) {
        this(type, null);
    }

    public Token(
            TokenType type,
            T value
    ) {
        this.type  = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public Token<T> located(StringLocation loc) {
        this.loc = loc;
        return this;
    }

    public Token<?> located(String fn, StringReader strReader, int si, int index) {
        return located(new StringLocation(fn, strReader, si, index));
    }

    public StringLocation getLocation() {
        return loc;
    }

    @SuppressWarnings("unchecked")
    public <T2> T2 getValue() {
        return (T2) value;
    }

    @SuppressWarnings("unchecked")
    public <T2> T2 getValue(Class<T2> t2Class) {
        return (T2) value;
    }

    @Override
    public String toString() {
        return "Tk" + type.name() + (value != null ? "(" + value + ")" : "");
    }

}
