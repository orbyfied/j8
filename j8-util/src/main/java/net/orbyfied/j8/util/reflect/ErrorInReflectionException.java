package net.orbyfied.j8.util.reflect;

public class ErrorInReflectionException extends RuntimeException {

    public ErrorInReflectionException() {
    }

    public ErrorInReflectionException(String message) {
        super(message);
    }

    public ErrorInReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErrorInReflectionException(Throwable cause) {
        super(cause);
    }

    public ErrorInReflectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
