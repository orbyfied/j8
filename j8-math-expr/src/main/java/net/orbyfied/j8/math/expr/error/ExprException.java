package net.orbyfied.j8.math.expr.error;

import net.orbyfied.j8.math.expr.StringLocation;

import java.io.PrintStream;

public abstract class ExprException extends RuntimeException {

    public static void printFancy(PrintStream stream, Exception e, boolean debug) {
        stream.println("\u001B[31m" + e.getClass().getSimpleName() + ": " + e.getMessage() + "\u001B[0m");
        StringLocation loc;
        if (e instanceof ExprException le && (loc = le.getLocation()) != null) {
            stream.println("\u001B[31m  " + loc.toStringFancy(10, true) + "\u001B[0m");
        }

        if (debug) {
            for (StackTraceElement elem : e.getStackTrace()) {
                stream.println("\u001B[31m   at " + elem + "\u001B[0m");
            }
        }
    }

    //////////////////////////////////////////////////////////

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
