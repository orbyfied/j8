package net.orbyfied.j8.util.reflect;

public class ReflectorFail {

    // the java errors (optional)
    Throwable[] throwables;

    // the message
    String message;

    // the reflector
    Reflector reflector;

    public ReflectorFail(Reflector reflector) {
        this.reflector = reflector;
    }

    /* Setters */

    public ReflectorFail withMessage(String message) {
        this.message = message;
        return this;
    }

    public ReflectorFail withThrowables(Throwable... throwables) {
        this.throwables = throwables;
        return this;
    }

    /* Getters */

    public Throwable[] getThrowables() {
        return throwables;
    }

    public String getMessage() {
        return message;
    }

    public Reflector getReflector() {
        return reflector;
    }

}
