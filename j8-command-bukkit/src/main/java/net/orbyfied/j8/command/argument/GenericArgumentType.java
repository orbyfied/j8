package net.orbyfied.j8.command.argument;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.SuggestionAccumulator;
import net.orbyfied.j8.command.argument.options.ArgumentOptions;
import net.orbyfied.j8.util.StringReader;

import java.util.*;

/**
 * @param <B> The base type (without the generics, for example {@link List})
 */
public abstract class GenericArgumentType<B> implements ArgumentType<B> {

    public GenericArgumentType(List<String> params) {
        this.parameters = new ArrayList<>(params);
    }

    public GenericArgumentType(String... params) {
        this(Arrays.asList(params));
    }

    /**
     * The type parameters available.
     */
    final ArrayList<String> parameters;

    @SuppressWarnings("unchecked")
    public Class<B> getBaseType() {
        return (Class<B>) getType();
    }

    public TypeIdentifier getGenericIdentifier(LinkedHashMap<String, ArgumentType<?>> typeParams) {
        TypeIdentifier id = getBaseIdentifier().clone();
        for (ArgumentType<?> pt : typeParams.values())
            id.getTypeParameters().add(pt.getIdentifier());
        return id;
    }

    public List<String> getTypeParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public String getTypeParameter(int i) {
        return parameters.get(i);
    }

    @Override
    public TypeIdentifier getIdentifier() {
        return getBaseIdentifier();
    }

    @Override
    public boolean accepts(Context context, StringReader reader) {
        throw new IllegalArgumentException("Raw use of parameterized type " + getBaseIdentifier());
    }

    @Override
    public B parse(Context context, StringReader reader) {
        throw new IllegalArgumentException("Raw use of parameterized type " + getBaseIdentifier());
    }

    @Override
    public void write(Context context, StringBuilder builder, B v) {
        throw new IllegalArgumentException("Raw use of parameterized type " + getBaseIdentifier());
    }

    @Override
    public void suggest(Context context, SuggestionAccumulator suggestions) {
        throw new IllegalArgumentException("Raw use of parameterized type " + getBaseIdentifier());
    }

    public GenericTypeInstance<B> instance(ArgumentType... types) {
        return new GenericTypeInstance<>(this, types);
    }

    public GenericTypeInstance<B> instance(List<ArgumentType> types) {
        return new GenericTypeInstance<>(this, types);
    }

    /* actual parameter methods */

    public abstract boolean accepts(Context context, StringReader reader, LinkedHashMap<String, ArgumentType> types);
    public abstract B parse(Context context, StringReader reader, LinkedHashMap<String, ArgumentType> types);
    public abstract void write(Context context, StringBuilder builder, B v, LinkedHashMap<String, ArgumentType> types);
    public abstract void suggest(Context context, SuggestionAccumulator suggestions, LinkedHashMap<String, ArgumentType> types);

}
