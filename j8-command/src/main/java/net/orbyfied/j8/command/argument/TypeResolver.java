package net.orbyfied.j8.command.argument;

import net.orbyfied.j8.registry.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that resolves and compiles {@link ArgumentType}s.
 */
public interface TypeResolver {

    static TypeResolver memoryBacked() {
        return new TypeResolver() {
            // the internal map backing it
            Map<Identifier, ArgumentType<?>> map = new HashMap<>();

            @Override
            public void register(ArgumentType<?> type) {
                map.put(type.getBaseIdentifier(), type);
            }

            @Override
            public ArgumentType<?> resolve(Identifier identifier) {
                return map.get(identifier);
            }
        };
    }

    ///////////////////////////////////////////

    /**
     * Registers the given type if
     * this resolver allows it.
     * @throws UnsupportedOperationException If the resolver is immutable.
     * @param type The type to register.
     */
    default void register(ArgumentType<?> type) { throw new UnsupportedOperationException("Resolver is immutable"); }

    /**
     * Resolves a type by identifier.
     * @param identifier The identifier.
     * @return The type or null if not found.
     */
    ArgumentType<?> resolve(Identifier identifier);

    /**
     * Compiles a type identifier into an argument type
     * object.
     *
     * By default it resolves the base type, and
     * compiles generic type arguments if needed, then includes
     * them and returns the result.
     * @param identifier The type identifier/descriptor.
     * @return The argument type or null if not found/valid.
     */
    @SuppressWarnings("rawtypes")
    default ArgumentType<?> compile(TypeIdentifier identifier) {
        // resolve base type
        ArgumentType<?> base = resolve(identifier);
        // return immediately if it is not
        // a generic type, because we dont
        // need to compile any type arguments
        if (!(base instanceof GenericArgumentType<?> g))
            return base;

        // get type parameters as ids
        List<TypeIdentifier> params = identifier.getTypeParameters();
        // create list of type parameters as instances
        List<ArgumentType> types = new ArrayList<>(params.size());
        // loop over ids and compile them
        int l = params.size();
        for (int i = 0; i < l; i++)
            types.add(
                    compile(params.get(i))
            );

        // return a generic instance with
        // the compiled types
        return g.instance(types);
    }

}
