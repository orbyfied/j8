package net.orbyfied.j8.util.math.expr.error;

public class ExprInterpreterException extends RuntimeException {

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

}
