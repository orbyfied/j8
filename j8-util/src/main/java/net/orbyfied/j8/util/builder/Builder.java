package net.orbyfied.j8.util.builder;

/**
 * A class responsible for configuring
 * and building an instance of T.
 * @param <T> The target instance type.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Builder<T, S extends Builder> {

    public static <V> Raw<V> raw(BuilderTemplate<V, Raw<V>> template) {
        return new Raw<>(template);
    }

    public static class Raw<T> extends Builder<T, Raw<T>> {
        public Raw(BuilderTemplate<T, Raw<T>> template) {
            super(template);
        }
    }

    /////////////////////////////////////

    // template
    final BuilderTemplate<T, S> template;

    // runtime type
    final Class<T> type;

    // values
    final Values parameters;

    // self
    final S self;

    {
        this.self = (S) this;
    }

    public Builder(BuilderTemplate<T, S> template) {
        this.template   = template;
        this.type       = template.type();
        this.parameters = template.parameters().blank();
    }

    @SuppressWarnings("unchecked")
    public T build() {
        // find constructor
        Constructor<T, S> constructor = template.findConstructor(self);

        // call constructor
        T t = constructor.construct(self);

        // return
        return t;
    }

    /* Parameters. */

    public S set(String name, Object val) {
        parameters.set(name, val);
        return self;
    }

    public S unset(String name) {
        parameters.unset(name);
        return self;
    }

    public <V> V get(String name) {
        return (V) parameters.get(name);
    }

    public S ignore(String name) {
        parameters.mark("ignored", parameters.property(name), true);
        return self;
    }

}
