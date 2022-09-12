package net.orbyfied.j8.util.math.expr.error;

import net.orbyfied.j8.util.math.expr.StringLocation;

public class ExprInterpreterException extends RuntimeException implements LocatedException {

    StringLocation loc;

    public ExprInterpreterException() {
        super();
    }

    public ExprInterpreterException(String message) {
        super(message);
    }

    public ExprInterpreterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExprInterpreterException(Throwable cause) {
        super(cause);
    }

    @Override
    public StringLocation getLocation() {
        return loc;
    }

    @Override
    public ExprInterpreterException located(StringLocation loc) {
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
