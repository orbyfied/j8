package net.orbyfied.j8.util.math.expr.error;

import net.orbyfied.j8.util.math.expr.StringLocation;

public class ExprParserException extends RuntimeException implements LocatedException {

    StringLocation loc;

    public ExprParserException(String message) {
        super(message);
    }

    public ExprParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExprParserException(Throwable cause) {
        super(cause);
    }

    @Override
    public StringLocation getLocation() {
        return loc;
    }

    @Override
    public ExprParserException located(StringLocation loc) {
        this.loc = loc;
        return this;
    }

    @Override
    public String getMessage() {
        StringBuilder b = new StringBuilder();
        if (loc != null)
            b.append(loc.toStringFancy()).append(" ");
        b.append(super.getMessage());
        return b.toString();
    }
}
