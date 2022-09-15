package net.orbyfied.j8.util.builder;

import java.util.*;

/**
 * A class responsible for specifying the instance
 * parameters for T to a builder of T.
 * @param <T> The target instance type.
 * @param <B> The builder type for T.
 */
@SuppressWarnings("rawtypes")
public class BuilderTemplate<T, B extends Builder> {

    // runtime type
    private final Class<T> type;

    // parameters
    private final Properties params = new Properties("%");

    // constructors
    private final List<Constructor<T, B>> constructors = new ArrayList<>();

    public BuilderTemplate(Class<T> type) {
        this.type = type;
    }

    public Constructor<T, B> findConstructor(B builder) {
        // test all constructors and find first match
        Constructor<T, B> constructor = null;
        for (Constructor<T, B> c : constructors) {
            if (c.test(builder)) {
                constructor = c;
                break;
            }
        }

        // return
        return constructor;
    }

    public Class<T> type() {
        return type;
    }

    /* Getter. */

    public <V> Property<V> parameter(String name) {
        return params.get(name);
    }

    public Properties parameters() {
        return params;
    }

    /* Configure. */

    public BuilderTemplate<T, B> parameter(String name, Property<?> property) {
        // register
        params.add(name, property);

        // return
        return this;
    }

    @SafeVarargs
    public final BuilderTemplate<T, B> constructors(Constructor<T, B>... c) {
        // add all
        constructors.addAll(Arrays.asList(c));

        // return
        return this;
    }

}
