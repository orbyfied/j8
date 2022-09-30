package net.orbyfied.j8.math.expr.error;

public class SyntaxError extends ExprParserException {

    public SyntaxError(String message) {
        super(message);
    }

    public SyntaxError(String message, Throwable cause) {
        super(message, cause);
    }

    public SyntaxError(Throwable cause) {
        super(cause);
    }

}
