package net.orbyfied.j8.expr.error;

import net.orbyfied.j8.expr.StringLocation;

public class ExprCompilerException extends ExprException {

    public ExprCompilerException() {
    }

    public ExprCompilerException(String message) {
        super(message);
    }

    public ExprCompilerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExprCompilerException(Throwable cause) {
        super(cause);
    }

    StringLocation loc;

    @Override
    public StringLocation getLocation() {
        return loc;
    }

    @Override
    public ExprCompilerException located(StringLocation loc) {
        this.loc = loc;
        return this;
    }

}
