package net.orbyfied.j8.command.impl;

import net.orbyfied.j8.command.parameter.ParameterType;
import net.orbyfied.j8.command.parameter.TypeResolver;
import net.orbyfied.j8.registry.Identifier;

import java.util.HashMap;

public class DelegatingNamespacedTypeResolver implements TypeResolver {

    TypeResolver miscDelegate;
    HashMap<String, TypeResolver> namespaces = new HashMap<>();

    public DelegatingNamespacedTypeResolver delegate(TypeResolver miscDelegate) {
        this.miscDelegate = miscDelegate;
        return this;
    }

    public TypeResolver delegate() {
        return miscDelegate;
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
    public ParameterType<?> resolve(Identifier identifier) {
        if (identifier.getNamespace() == null || identifier.getNamespace().isEmpty())
            identifier = new Identifier("system", identifier.getPath());
        TypeResolver namespacedResolver = namespaces.get(identifier.getNamespace());
        if (namespacedResolver != null)
            return namespacedResolver.resolve(identifier);
        if (miscDelegate != null)
            return miscDelegate.resolve(identifier);
        return null;
    }

}
