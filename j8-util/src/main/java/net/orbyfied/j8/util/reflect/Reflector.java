package net.orbyfied.j8.util.reflect;

import net.orbyfied.j8.util.functional.ThrowableRunnable;
import net.orbyfied.j8.util.functional.ThrowableSupplier;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class Reflector {

    public static final Consumer<ReflectorFail> FAIL_HANDLER_RETHROW = fail -> {
        if (fail.throwables.length == 0)
            throw new ErrorInReflectionException(fail.message);
        throw new ErrorInReflectionException(fail.message, fail.throwables[0]);
    };

    static final DateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm:ss.SSSS");

    public static final Consumer<ReflectorFail> FAIL_HANDLER_PRINT = fail -> {
        System.err.println("(" + DATE_FORMAT.format(new Date()) + ") REFLECT FAIL '" + fail.reflector.name + "'" +
                (fail.message != null ? " - " + fail.message : ""));
        for (Throwable t : fail.throwables)
            t.printStackTrace();
    };

    //////////////////////////////////////////

    // name
    final String name;
    // fail handler
    Consumer<ReflectorFail> handler = FAIL_HANDLER_RETHROW;

    // method handle lookup
    MethodHandles.Lookup mhLookup = MethodHandles.lookup();

    public Reflector(String name) {
        this.name = name;
    }

    /**
     * Register a failure to the handler.
     * @param fail The fail.
     */
    public void fail(ReflectorFail fail) {
        handler.accept(fail);
    }

    // do under protection of
    // fail handler
    private <T> T doSafe(String message, ThrowableSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable t) {
            // create fail and register
            fail(new ReflectorFail(this)
                    .withThrowables(t)
                    .withMessage("Error: " + message));

            // return nothing
            return null;
        }
    }

    private void doSafe(String message, ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            // create fail and register
            fail(new ReflectorFail(this)
                    .withThrowables(t)
                    .withMessage("Error: " + message));
        }
    }

    /* ---- Core ---- */

    public <T extends AccessibleObject> T accessible(T t) {
        return doSafe("set accessible", () -> {
            t.setAccessible(true);
            return t;
        });
    }

    public Class<?> classForName(final String name) {
        return doSafe("reflect class for name '" + name + "'", () -> Class.forName(name));
    }

    public Class<?> classForName(final String name, final boolean init, final ClassLoader loader) {
        return doSafe("reflect class for name '" + name + "'", () -> Class.forName(name, init, loader));
    }

    public MethodHandle virtualMethodHandle(final Class<?> klass, final String name, final Class<?>[] types) {
        return doSafe("get method handle '" + name + "' on '" + klass.getName() + "'",
                () -> mhLookup.findVirtual(klass, name, MethodType.methodType(Object.class, types)));
    }

    public MethodHandle staticMethodHandle(final Class<?> klass, final String name, final Class<?>[] types) {
        return doSafe("get method handle '" + name + "' on '" + klass.getName() + "'",
                () -> mhLookup.findStatic(klass, name, MethodType.methodType(Object.class, types)));
    }

    public Method reflectMethod(final Class<?> klass, final String name, final Class<?>[] types) {
        return doSafe("reflect method '" + name + "' from '" + klass.getName() + "'",
                () -> klass.getMethod(name, types));
    }

    public Method reflectDeclaredMethod(final Class<?> klass, final String name, final Class<?>[] types) {
        return doSafe("reflect method '" + name + "' from '" + klass.getName() + "'",
                () -> klass.getDeclaredMethod(name, types));
    }

    public Method reflectMethodAccessible(final Class<?> klass, final String name, final Class<?>[] types) {
        return accessible(reflectMethod(klass, name, types));
    }

    public Method reflectDeclaredMethodAccessible(final Class<?> klass, final String name, final Class<?>[] types) {
        return accessible(reflectDeclaredMethod(klass, name, types));
    }

    @SuppressWarnings("unchecked")
    public <T> Constructor<T> reflectConstructor(final Class<?> klass, final Class<?>[] types) {
        return (Constructor<T>) doSafe("reflect constructor for '" + klass.getName() + "'",
                (() -> klass.getConstructor(types)));
    }

    @SuppressWarnings("unchecked")
    public <T> Constructor<T> reflectDeclaredConstructor(final Class<?> klass, final Class<?>[] types) {
        return (Constructor<T>) doSafe("reflect constructor for '" + klass.getName() + "'",
                (() -> klass.getConstructor(types)));
    }

    public Field reflectField(final Class<?> klass, final String name) {
        return doSafe("reflect method '" + name + "' from '" + klass.getName() + "'",
                () -> klass.getField(name));
    }

    public Field reflectDeclaredField(final Class<?> klass, final String name) {
        return doSafe("reflect method '" + name + "' from '" + klass.getName() + "'",
                () -> klass.getDeclaredField(name));
    }

    public Field reflectFieldAccessible(final Class<?> klass, final String name) {
        return accessible(reflectField(klass, name));
    }

    public Field reflectDeclaredFieldAccessible(final Class<?> klass, final String name) {
        return accessible(reflectDeclaredField(klass, name));
    }

    @SuppressWarnings("unchecked")
    public <T> T reflectGetField(final Field field, final Object on) {
        return doSafe("get field '" + field + "'",
                () -> (T) field.get(on));
    }

    public void reflectSetField(final Field field, final Object on, final Object val) {
        doSafe("set field '" + field + "'",
                () -> field.set(on, val));
    }

    @SuppressWarnings("unchecked")
    public <T> T reflectInvoke(final Method method, final Object on, final Object... args) {
        return (T) doSafe("invoke method '" + method + "'",
                () -> method.invoke(on, args));
    }

    @SuppressWarnings("unchecked")
    public <T> T methodInvoke(final MethodHandle method, final Object on, final Object... args) {
        return (T) doSafe("invoke method '" + method + "'",
                () -> method.invoke(on, args));
    }

}
