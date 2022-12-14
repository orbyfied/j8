package net.orbyfied.j8.registry;

import net.orbyfied.j8.util.functional.Accumulator;
import net.orbyfied.j8.util.functional.KeyProvider;
import net.orbyfied.j8.util.functional.ValueProvider;
import net.orbyfied.j8.util.functional.EntryOperation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class for mapped and linear storage
 * of uniquely identified items using
 * an {@link Identifier}. Utilizes a
 * {@link HashMap} and
 * {@link ArrayList} for
 * mapped and linear storage respectively.
 * Every registry has it's own identifier as well.
 * But most importantly, it allows for the addition of
 * mapping and listing modules and services to add functionality.
 * @param <T> The type of registry item.
 *            Restricted to ? extends Identifiable
 */
public class Registry<T extends Identifiable>
        implements Identifiable, Iterable<T>,
        KeyProvider<Identifier>, ValueProvider<T> {

    /* ---- ITEMS ---- */

    /**
     * The mapped item storage.
     * Maps the items by identifier.
     * Useful for lookups and removals.
     */
    private final HashMap<Identifier, T> mapped = new HashMap<>();

    /**
     * The linear item storage.
     * Stores the items in a list.
     * Useful for iteration.
     */
    private final ArrayList<T> linear = new ArrayList<>();

    /* ---- MODULES ---- */

    /**
     * The linear component storage.
     */
    private final ArrayList<RegistryComponent<Registry<T>, T, ?, ?>> componentsLinear = new ArrayList<>();

    /**
     * The mapped component storage.
     */
    private final HashMap<Class<? extends RegistryComponent<Registry<T>, T, ?, ?>>, RegistryComponent<Registry<T>, T, ?, ?>> componentsMapped = new HashMap<>();

    /* ---- SERVICES ---- */

    /**
     * The linear service storage.
     */
    private final ArrayList<RegistryService<Registry<T>, T>> servicesLinear = new ArrayList<RegistryService<Registry<T>, T>>();

    /**
     * The mapped service storage.
     */
    private final HashMap<Class<? extends RegistryService<Registry<T>, T>>, RegistryService<Registry<T>, T>> servicesMapped = new HashMap<Class<? extends RegistryService<Registry<T>, T>>, RegistryService<Registry<T>, T>>();

    /* ---- MISC ---- */

    /**
     * Stores all components and services mapped to
     * their runtime key type.
     */
    private final HashMap<Class<?>, Object> suppliersByKeyType = new HashMap<>();

    /* ---- THIS ---- */

    /**
     * The unique identifier of this registry.
     */
    private final Identifier identifier;

    /**
     * The runtime type of T.
     */
    private final Class<T> runtimeType;

    /**
     * Base constructor.
     * @param identifier The identifier of this registry.
     */
    @SuppressWarnings("unchecked")
    public Registry(Identifier identifier, Class<?> runtimeType) {
        this.identifier  = identifier;
        this.runtimeType = (Class<T>) runtimeType;
    }

    /**
     * Base constructor.
     * @param identifier The identifier in string form.
     */
    public Registry(String identifier, Class<?> runtimeType) {
        this(Identifier.of(identifier), runtimeType);
    }

    /**
     * @see Identifiable#getIdentifier()
     * @return The unique identifier of this registry.
     */
    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Gets the size of the registry.
     * @return The size.
     */
    public int size() {
        return linear.size();
    }

    /**
     * Gets an item by index.
     * @param i The index.
     * @return The item.
     */
    public T getByIndex(int i) {
        return linear.get(i);
    }

    /**
     * Gets an item by identifier.
     * @param id The identifier.
     * @return The item.
     */
    public T getByIdentifier(Identifier id) {
        return mapped.get(id);
    }

    /**
     * Gets an item by identifier.
     * @param id The identifier as string.
     * @return The item.
     */
    @SuppressWarnings("unchecked")
    public <R extends T> R getByIdentifier(String id) {
        return (R) getByIdentifier(Identifier.of(id));
    }

    /**
     * Registers a new item to the registry.
     * @param item The item to register.
     * @return This.
     */
    public Registry<T> register(T item) {
        // add to list and map
        mapped.put(item.getIdentifier(), item);
        linear.add(item);

        {
            int l;

            // go over all modules and apply
            l = componentsLinear.size();
            for (int i = 0; i < l; i++)
                componentsLinear.get(i).register(item);

            // go over functional services and apply
            l = servicesLinear.size();
            for (int i = 0; i < l; i++) {
                RegistryService<Registry<T>, T> service = servicesLinear.get(i);
                if (service instanceof FunctionalService<Registry<T>, T> fs)
                    fs.registered(item);
            }
        }

        // return
        return this;
    }

    /**
     * Unregisters an item from the registry.
     * @param item The item to remove.
     * @return This.
     */
    public Registry<T> unregister(T item) {
        // remove from list and map
        mapped.remove(item.getIdentifier());
        linear.remove(item);

        {
            int l;

            // go over all modules and apply
            l = componentsLinear.size();
            for (int i = 0; i < l; i++)
                componentsLinear.get(i).unregister(item);

            // go over functional services and apply
            l = servicesLinear.size();
            for (int i = 0; i < l; i++) {
                RegistryService<Registry<T>, T> service = servicesLinear.get(i);
                if (service instanceof FunctionalService<Registry<T>, T> fs)
                    fs.unregistered(item);
            }
        }

        // return
        return this;
    }

    /**
     * Unregisters an item from the registry.
     * @param id The identifier of the item to remove.
     * @return This.
     */
    public Registry<T> unregister(Identifier id) {
        return unregister(getByIdentifier(id));
    }

    /**
     * Unregisters an item from the registry.
     * @param id The identifier of the item to remove as string.
     * @return This.
     */
    public Registry<T> unregister(String id) {
        return unregister(getByIdentifier(id));
    }

    /**
     * @return The immutable list of items.
     */
    public List<T> linear() {
        return Collections.unmodifiableList(linear);
    }

    /**
     * @return The immutable map of identifiers to items.
     */
    public Map<Identifier, T> mapped() {
        return Collections.unmodifiableMap(mapped);
    }

    @SuppressWarnings("unchecked")
    public <K, R> R getValue(K key) {
        // get key type
        Class<?> klass = key.getClass();

        // get component
        Object o = suppliersByKeyType.get(klass);

        if (o == null)
            return null;

        if (o instanceof RegistryComponent component)
            return (R) component.getMapped(key);

        if (o instanceof MappingService service)
            return (R) service.getByKey(key);

        return null;
    }

    public EntryOperation<Registry<T>, Identifier, T> entry(T val) {
        return EntryOperation.<Registry<T>, Identifier, T>builder()
                .key(val.getIdentifier()).value(val)
                .returns(this)
                .doWith((id, t) -> register(t))
                .doWithout((id, t) -> unregister(t))
                .doGet(this::getByIdentifier)
                .doHas((id, t) -> has(t))
                .build();

    }

    public boolean has(Identifier id) {
        return mapped.containsKey(id);
    }

    public boolean has(T t) {
        if (t == null)
            return false;
        Object o = mapped.get(t.getIdentifier());
        if (o == null)
            return false;
        return t.equals(o);
    }

    @SuppressWarnings("unchecked")
    public <K, V, M extends RegistryComponent<Registry<T>, T, K, V>> M getComponent(int index) {
        return (M) componentsLinear.get(index);
    }

    @SuppressWarnings("unchecked")
    public <K, V, M extends RegistryComponent<Registry<T>, T, K, V>> M getComponent(Class<M> klass) {
        return (M) componentsMapped.get(klass);
    }

    @SuppressWarnings("unchecked")
    public Registry<T> addComponent(RegistryComponent<Registry<T>, T, ?, ?> module) {
        Objects.requireNonNull(module, "module cannot be null");
        componentsLinear.add(module);
        componentsMapped.put((Class<? extends RegistryComponent<Registry<T>, T, ?, ?>>) module.getClass(), module);
        suppliersByKeyType.put(module.getKeyType(), module);
        return this;
    }

    public Registry<T> addComponent(Function<Registry<T>, RegistryComponent<Registry<T>, T, ?, ?>> f) {
        Objects.requireNonNull(f, "function cannot be null");
        return addComponent(f.apply(this));
    }

    public Registry<T> addComponent(Class<? extends RegistryComponent<Registry<T>, T, ?, ?>> of) {
        try {
            Constructor<? extends RegistryComponent<Registry<T>, T, ?, ?>> constructor = of.getConstructor(Registry.class);
            return addComponent(constructor.newInstance(this));
        } catch (Exception e) {
            throw new RuntimeException("failed to construct and register module", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <K, V, M extends RegistryComponent<Registry<T>, T, K, V>> Registry<T> addComponent(
            Class<M> of, Consumer<M> consumer) {
        try {
            Constructor<? extends RegistryComponent<Registry<T>, T, ?, ?>> constructor = of.getConstructor(Registry.class);
            M m = (M) constructor.newInstance(this);
            addComponent(m);
            consumer.accept(m);
        } catch (Exception e) {
            throw new RuntimeException("failed to construct and register module", e);
        }

        return this;
    }

    public Registry<T> removeComponent(RegistryComponent<Registry<T>, T, ?, ?> module) {
        if (module == null)
            return this;
        componentsMapped.remove(module.getClass(), module);
        componentsLinear.remove(module);
        suppliersByKeyType.remove(module.getKeyType(), module);
        return this;
    }

    public Registry<T> removeComponent(Class<? extends RegistryComponent<Registry<T>, T, ?, ?>> klass) {
        if (klass == null)
            return this;
        return removeComponent(componentsMapped.get(klass));
    }

    public Map<Class<? extends RegistryComponent<Registry<T>, T, ?, ?>>, RegistryComponent<Registry<T>, T, ?, ?>> getComponentsMapped() {
        return Collections.unmodifiableMap(componentsMapped);
    }

    public List<RegistryComponent<Registry<T>, T, ?, ?>> getComponentsLinear() {
        return Collections.unmodifiableList(componentsLinear);
    }

    public int getComponentsSize() {
        return componentsLinear.size();
    }

    @SuppressWarnings("unchecked")
    public <K, V, M extends RegistryService<Registry<T>, T>> M getService(int index) {
        return (M) servicesLinear.get(index);
    }

    @SuppressWarnings("unchecked")
    public <K, V, M extends RegistryService<Registry<T>, T>> M getService(Class<M> klass) {
        return (M) servicesMapped.get(klass);
    }

    @SuppressWarnings("unchecked")
    public Registry<T> addService(RegistryService<Registry<T>, T> service) {
        Objects.requireNonNull(service, "service cannot be null");
        servicesLinear.add(service);
        servicesMapped.put((Class<? extends RegistryService<Registry<T>, T>>) service.getClass(), service);
        if (service instanceof MappingService ms)
            suppliersByKeyType.put(ms.getKeyType(), ms);
        return this;
    }

    public Registry<T> addService(Function<Registry<T>, RegistryService<Registry<T>, T>> f) {
        Objects.requireNonNull(f, "function cannot be null");
        return addService(f.apply(this));
    }

    public Registry<T> addService(Class<? extends RegistryService<Registry<T>, T>> of) {
        try {
            Constructor<? extends RegistryService<Registry<T>, T>> constructor = of.getConstructor(Registry.class);
            return addService(constructor.newInstance(this));
        } catch (Exception e) {
            throw new RuntimeException("failed to construct and register service", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <K, V, M extends RegistryService<Registry<T>, T>> Registry<T> addService(
            Class<M> of, Consumer<M> consumer) {
        try {
            Constructor<? extends RegistryService<Registry<T>, T>> constructor = of.getConstructor(Registry.class);
            M m = (M) constructor.newInstance(this);
            addService(m);
            consumer.accept(m);
        } catch (Exception e) {
            throw new RuntimeException("failed to construct and register service", e);
        }

        return this;
    }

    public Registry<T> removeService(RegistryService<Registry<T>, T> service) {
        if (service == null)
            return this;
        servicesMapped.remove(service.getClass(), service);
        servicesLinear.remove(service);
        if (service instanceof MappingService ms)
            suppliersByKeyType.remove(ms.getKeyType(), ms);
        return this;
    }

    public Registry<T> removeService(Class<? extends RegistryService<Registry<T>, T>> klass) {
        if (klass == null)
            return this;
        return removeService(servicesMapped.get(klass));
    }

    @SuppressWarnings("unchecked")
    public <S> ArrayList<S> getServicesOf(Class<S> type,
                                          ArrayList<S> list) {
        for (Object o : servicesLinear)
            if (type.isAssignableFrom(o.getClass()))
                list.add((S) o);
        return list;
    }

    public <S> ArrayList<S> getServicesOf(Class<S> type) {
        return getServicesOf(type, new ArrayList<>());
    }

    public List<RegistryService<Registry<T>, T>> getServicesLinear() {
        return Collections.unmodifiableList(servicesLinear);
    }

    public Map<Class<? extends RegistryService<Registry<T>, T>>, RegistryService<Registry<T>, T>> getServicesMapped() {
        return Collections.unmodifiableMap(servicesMapped);
    }

    public int getServicesSize() {
        return servicesLinear.size();
    }

    /* ------ Auto Register ------ */

    private static final HashMap<Class<?>, BiFunction<Registry, Object, ? extends Identifiable>> fieldHandlers = new HashMap<>();

    static {
        fieldHandlers.put(Supplier.class, (registry, o) -> ((Supplier<? extends Identifiable>)o).get());
    }

    /**
     * Registers all registrable items which allow it
     * across the static fields and instance fields which
     * are retrieved for every instance.
     * @param klass The class.
     * @param instances The instances.
     * @return This.
     */
    @SuppressWarnings("unchecked")
    public Registry<T> autoRegisterFrom(Class<?> klass, Object... instances) {
        try {

            // go over every field
            for (Field field : klass.getDeclaredFields()) {
                // process annotation if present
                AutoRegister ann;
                if ((ann = field.getAnnotation(AutoRegister.class)) != null) {
                    // check allow
                    if (!ann.allow())
                        continue;

                    // check if it is auto register all
                    if (ann.all()) {
                        // set accessible and get
                        field.setAccessible(true);

                        // check modifiers
                        if (Modifier.isStatic(field.getModifiers())) {
                            // handle only static
                            if (ann.allStatic())
                                this.autoRegisterFrom(field.getType());
                            else
                                this.autoRegisterFrom(field.getType(), field.get(null));
                        } else {
                            // handle only static
                            if (ann.allStatic())
                                // only have to register once as it
                                // ignores the instance and only looks at the class
                                // which will of course always be the same
                                // across all instances
                                this.autoRegisterFrom(field.getType());
                            else
                                for (Object instance : instances)
                                    this.autoRegisterFrom(field.getType(), field.get(instance));
                        }

                        // skip rest
                        continue;
                    }
                }

                // check type
                Class<?> type = field.getType();
                boolean isT = runtimeType.isAssignableFrom(type);
                BiFunction<Registry, Object, ? extends Identifiable> handler = fieldHandlers.get(type);
                if (
                        !isT &&
                        handler == null
                )
                    continue;

                // set accessible
                field.setAccessible(true);

                // check modifiers
                if (Modifier.isStatic(field.getModifiers())) {
                    // handle once
                    if (isT)
                        this.register((T) field.get(null));
                    else
                        this.register((T) handler.apply(this, field.get(null)));
                } else {
                    // handle for every instance
                    if (isT)
                        for (Object instance : instances)
                            this.register((T) field.get(instance));
                    else
                        for (Object instance : instances)
                            this.register((T) handler.apply(this, field.get(instance)));
                }

            }

        } catch (Exception e) {
            // simply handle error
            // nothing special
            System.out.println("error while auto registering for class: " + klass + " with " + instances.length + " instances provided");
            e.printStackTrace();
        }

        // return
        return this;
    }

    ///////////////////////////////

    @Override
    public void provideKeys(Accumulator<Identifier> acc) {
        for (T v : linear)
            acc.add(v.getIdentifier());
    }

    @Override
    public void provideValues(Accumulator<T> acc) {
        for (T v : linear)
            acc.add(v);
    }

    class RegistryIterator implements Iterator<T> {
        int i = 0;

        @Override
        public boolean hasNext() {
            return i < linear.size();
        }

        @Override
        public T next() {
            return linear.get(i++);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new RegistryIterator();
    }

    @Override
    public String toString() {
        return "Registry(" + identifier + "): " + mapped;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Registry<?> registry = (Registry<?>) o;
        return Objects.equals(identifier, registry.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

}
