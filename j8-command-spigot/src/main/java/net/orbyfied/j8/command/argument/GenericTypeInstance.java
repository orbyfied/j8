package net.orbyfied.j8.command.argument;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.SuggestionAccumulator;
import net.orbyfied.j8.util.StringReader;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * An instance of a generic type with type
 * parameters specified. This instance is
 * usable as a type to be parse and whatever.
 * @param <B> The value type.
 */
@SuppressWarnings("rawtypes")
public class GenericTypeInstance<B> implements ArgumentType<B> {

    /**
     * Constructor.
     * @param base The base type.
     * @param types The type arguments.
     * @see GenericTypeInstance#GenericTypeInstance(GenericArgumentType, List)
     */
    public GenericTypeInstance(GenericArgumentType<B> base,
                               ArgumentType... types) {
        this(base, Arrays.asList(types));
    }

    /**
     * Constructor.
     * @param base The base type.
     * @param types The type arguments.
     */
    public GenericTypeInstance(GenericArgumentType<B> base,
                               List<ArgumentType> types) {
        // set base
        this.base = base;

        // set type parameter values
        int l = types.size();
        if (l != base.getTypeParameters().size())
            throw new IllegalArgumentException("Invalid amount of type parameters.");
        List<String> paramNames = base.getTypeParameters();
        for (int i = 0; i < l; i++) {
            params.put(paramNames.get(i), types.get(i));
        }
    }

    /**
     * The base type.
     */
    final GenericArgumentType<B> base;

    /**
     * The (ordered) map of type parameters by name.
     */
    final LinkedHashMap<String, ArgumentType> params = new LinkedHashMap<>();

    /**
     * @return The base type.
     */
    public GenericArgumentType<B> getBase() {
        return base;
    }

    /**
     * @return The parameterized type identifier.
     */
    @Override
    public TypeIdentifier getIdentifier() {
        TypeIdentifier id = getBaseIdentifier().clone();
        for (ArgumentType<?> pt : params.values())
            id.getTypeParameters().add(pt.getIdentifier());
        return id;
    }

    /**
     * @return The base type identifier.
     */
    @Override
    public TypeIdentifier getBaseIdentifier() {
        return base.getBaseIdentifier();
    }

    /**
     * @return The runtime value type.
     */
    @Override
    public Class<?> getType() {
        return base.getType();
    }

    /* Argument Type Implementation */

    @Override
    public boolean accepts(Context context, StringReader reader) {
        return base.accepts(context, reader, params);
    }

    @Override
    public B parse(Context context, StringReader reader) {
        return base.parse(context, reader, params);
    }

    @Override
    public void write(Context context, StringBuilder builder, B v) {
        base.write(context, builder, v, params);
    }

    @Override
    public void suggest(Context context, SuggestionAccumulator suggestions) {
        base.suggest(context, suggestions, params);
    }

}
