package net.orbyfied.j8.util.builder;

import java.lang.reflect.InaccessibleObjectException;
import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public abstract class Constructor<T, B extends Builder> {

    // builder-take constructor reflection cache
    private static final Map<Class<?>, java.lang.reflect.Constructor<?>>
            builderTakeConstructorCache = new HashMap<>();

    /**
     * Creates a constructor which passes the
     * builder instance to an instance constructor
     * of the class T.
     * @param tClass The runtime type of T.
     * @param <T> The target instance type.
     * @param <B> The builder type.
     * @return The constructor instance.
     */
    @SuppressWarnings("unchecked")
    public static <T, B extends Builder>
    Constructor<T, B> takeBuilder(Class<T> tClass) {
        // try to find constructor from cache
        java.lang.reflect.Constructor<T> constructor = (java.lang.reflect.Constructor<T>) builderTakeConstructorCache.get(tClass);

        // try to get constructor with reflection
        if (constructor == null) {
            try {
                // find constuctor
                for (java.lang.reflect.Constructor<?> c : tClass.getDeclaredConstructors()) {
                    // check is valid constructor
                    if (c.getParameterCount() == 1 &&
                            Builder.class.isAssignableFrom(c.getParameterTypes()[0])) {
                        // set and break
                        constructor = (java.lang.reflect.Constructor<T>) c;
                        break;
                    }
                }

                // check if we finally found something
                if (constructor == null) {
                    System.err.println("Could not find builder-take constructor for type " +
                            tClass.getName());
                    return null;
                }

                // set accessible
                constructor.setAccessible(true);
            } catch (InaccessibleObjectException e) {
                // constructor was inaccessible
                System.err.println("Could not access builder-take constructor for type " +
                        tClass.getName());
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                // unknown error
                System.err.println("Error finding builder-take constructor for type " +
                        tClass.getName());
                e.printStackTrace();
                return null;
            }
        }

        // cache found
        builderTakeConstructorCache.put(tClass, constructor);

        // copy to final
        final java.lang.reflect.Constructor<T> fConstructor = constructor;

        // create instance
        return new Constructor<>(tClass) {
            @Override
            public T construct(B builder) {
                try {
                    return fConstructor.newInstance(builder);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create instance of type " + tClass.getName() +
                            " using builder-take constructor.", e);
                }
            }
        };
    }

    //////////////////////////////

    public Constructor(Class<T> type) {
        this.type = type;
    }

    // the runtime type
    final Class<T> type;

    // predicates
    final List<Predicate<B>> predicates = new ArrayList<>();

    /**
     * Constructs a new instance of type T with
     * the information provided by the builder.
     * @param builder The builder.
     * @return The instance.
     */
    public abstract T construct(B builder);

    /* Setter. */

    @SafeVarargs
    public final Constructor<T, B> onlyIf(Predicate<B>... p) {
        predicates.addAll(Arrays.asList(p));
        return this;
    }

    /* Process. */

    public boolean test(B builder) {
        for (Predicate<B> predicate : predicates) {
            if (!predicate.test(builder))
                return false;
        }

        return true;
    }

}
