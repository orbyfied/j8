package net.orbyfied.j8.expr.error;

import net.orbyfied.j8.expr.util.StringLocation;

public class ExprInterpreterException extends ExprException {

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

}
