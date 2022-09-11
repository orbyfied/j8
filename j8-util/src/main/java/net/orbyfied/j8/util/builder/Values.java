package net.orbyfied.j8.util.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Values {

    // property registry
    final Properties properties;

    // values
    final Map<Property<?>, Object> values = new HashMap<>();

    // misc values
    final Map<String, Map<Property<?>, Object>> marks = new HashMap<>();

    public Values(Properties properties) {
        this.properties = properties;
    }

    /* Properties. */

    public Properties properties() {
        return properties;
    }

    public <T> Property<T> property(String name) {
        return properties.get(name);
    }

    /* Values. */

    @SuppressWarnings("unchecked")
    public <T> T get(Property<T> property) {
        return (T) values.get(property);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) values.get(property(name));
    }

    public <T> Values set(Property<?> property, T val) {
        if (val != null) {
            Class<?> tClass = val.getClass();
            if (!property.type.isAssignableFrom(tClass))
                throw new ClassCastException("Cannot apply value of type " + tClass.getName() +
                        " to property of type " + property.type.getName());
        }
        values.put(property, val);
        return this;
    }

    public <T> Values set(String name, T val) {
        return set(property(name), val);
    }

    public Values unset(Property<?> property) {
        values.remove(property);
        return this;
    }

    public Values unset(String name) {
        return unset(property(name));
    }

    /* Marks */

    private Map<Property<?>, Object> utilGetMark(String space) {
        return marks.computeIfAbsent(space, key -> new HashMap<>());
    }

    private Values utilUseMark(String space, Consumer<Map<Property<?>, Object>> consumer) {
        consumer.accept(utilGetMark(space));
        return this;
    }

    public Values mark(String space, Property<?> property, Object value) {
        return utilUseMark(space, map -> map.put(property, value));
    }

    @SuppressWarnings("unchecked")
    public <T> T marked(String space, Property<?> property) {
        return (T) utilGetMark(space).get(property);
    }

    @SuppressWarnings("unchecked")
    public Values unmark(String space, Property<?> property) {
        return utilUseMark(space, map -> map.remove(property));
    }

}
