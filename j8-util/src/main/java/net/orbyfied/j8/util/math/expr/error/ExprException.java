package net.orbyfied.j8.util.math.expr.error;

import net.orbyfied.j8.util.math.expr.StringLocation;

public abstract class ExprException extends RuntimeException {

    public ExprException() {
    }

    public ExprException(String message) {
        super(message);
    }

    public ExprException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExprException(Throwable cause) {
        super(cause);
    }

    public abstract StringLocation getLocation();

    public abstract ExprException located(StringLocation loc);

}
