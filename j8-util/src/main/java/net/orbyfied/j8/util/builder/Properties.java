package net.orbyfied.j8.util.builder;

import java.util.*;

public class Properties {

    // registry
    final List<Property<?>>        properties  = new ArrayList<>();
    final Map<String, Property<?>> propertyMap = new HashMap<>();

    // prefix
    final String prefix;

    public Properties(String prefix) {
        this.prefix = prefix;
    }

    /* Getter. */

    public String prefix() {
        return prefix;
    }

    /* Modify. */

    public List<Property<?>> getAll() {
        return Collections.unmodifiableList(properties);
    }

    public <T> Property<T> get(String name) {
        return getExact(prefix + name);
    }

    @SuppressWarnings("unchecked")
    public <T> Property<T> getExact(String id) {
        return (Property<T>) propertyMap.get(id);
    }

    public Properties remove(Property<?> property) {
        properties.remove(property);
        propertyMap.remove(property.name);
        return this;
    }

    public Properties remove(String name) {
        return remove(get(name));
    }

    public Properties removeExact(String name) {
        return remove(getExact(name));
    }

    public Properties addExact(Property<?> property) {
        properties.add(property);
        propertyMap.put(property.name, property);
        return this;
    }

    public Properties add(String name, Property<?> property) {
        return addExact(property.named(prefix + name));
    }

    /* Values. */

    public Values blank() {
        return new Values(this);
    }

    public Values defaults() {
        // create blank
        Values values = blank();

        // for each property
        for (Property<?> property : properties) {
            if (property.defaulted()) {
                values.set(property, property.defaultValue());
            }
        }

        // return
        return values;
    }

}
