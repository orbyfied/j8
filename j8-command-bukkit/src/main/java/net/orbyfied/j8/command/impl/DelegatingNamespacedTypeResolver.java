package net.orbyfied.j8.command.impl;

import net.orbyfied.j8.command.argument.ArgumentType;
import net.orbyfied.j8.command.argument.TypeResolver;
import net.orbyfied.j8.registry.Identifier;

import java.util.HashMap;

public class DelegatingNamespacedTypeResolver implements TypeResolver {

    /**
     * The delegate if no type could be found in either
     * the namespaced type resolver (if present), or the
     * flat map.
     */
    TypeResolver delegate;

    /**
     * The namespaced resolver map.
     */
    final HashMap<String, TypeResolver> namespaces = new HashMap<>();

    /**
     * The flat type map, to simply register and resolve
     * types from a specific identifier.
     */
    final HashMap<Identifier, ArgumentType<?>> flat = new HashMap<>();

    /**
     * Set the type resolver to delegate to.
     * @param delegate The delegate to set.
     * @return This.
     */
    public DelegatingNamespacedTypeResolver delegate(TypeResolver delegate) {
        this.delegate = delegate;
        return this;
    }

    /**
     * Get the delegate type resolver.
     * @return That.
     */
    public TypeResolver delegate() {
        return delegate;
    }

    /**
     * Register a flat ID to type mapping.
     * @param id The identifier. (key)
     * @param type The type. (value)
     * @return This.
     */
    public DelegatingNamespacedTypeResolver flat(Identifier id, ArgumentType<?> type) {
        this.flat.put(id, type);
        return this;
    }

    /**
     * @see DelegatingNamespacedTypeResolver#flat(Identifier, ArgumentType)
     */
    public DelegatingNamespacedTypeResolver flat(String id, ArgumentType<?> type) {
        return flat(Identifier.of(id), type);
    }

    /**
     * Get a type from the flat ID to type map.
     * @param id The identifier.
     * @return The type or null if absent.
     */
    public ArgumentType<?> flat(Identifier id) {
        return flat.get(id);
    }

    /**
     * @see DelegatingNamespacedTypeResolver#flat(Identifier)
     */
    public ArgumentType<?> flat(String id) {
        return flat(Identifier.of(id));
    }

    public DelegatingNamespacedTypeResolver namespace(String name, TypeResolver resolver) {
        this.namespaces.put(name, resolver);
        return this;
    }

    public TypeResolver namespace(String name) {
        return namespaces.get(name);
    }

    public DelegatingNamespacedTypeResolver removeNamespace(String name) {
        namespaces.remove(name);
        return this;
    }

    @Override
    public ArgumentType<?> resolve(Identifier identifier) {
        if (identifier.getNamespace() == null || identifier.getNamespace().isEmpty())
            identifier = new Identifier("system", identifier.getPath());
        ArgumentType<?> type = null;
        TypeResolver namespacedResolver = namespaces.get(identifier.getNamespace());
        if (namespacedResolver != null)
            type = namespacedResolver.resolve(identifier);
        if (type == null)
            type = flat.get(identifier);
        if (type == null && delegate != null)
            type = delegate.resolve(identifier);
        return type;
    }

}
