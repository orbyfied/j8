package net.orbyfied.j8.util.math.expr;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Context {

    public static Context newGlobal() {
        Context ctx = new Context(null, null);
        ctx.global = ctx;
        return ctx;
    }

    //////////////////////////////////////////

    protected Context(Context parent, Context global) {
        this.parent = parent;
        this.global = global;
    }

    /**
     * The parent of this context.
     */
    Context parent;

    /**
     * The global context.
     */
    Context global;

    /**
     * The global values.
     */
    Map<ExpressionValue<?>, ExpressionValue<?>> values = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <V> ExpressionValue<V> getValue(ExpressionValue<?> key) {
        // inquire local map
        if (values.containsKey(key))
            return (ExpressionValue<V>) values.get(key);
        // inquire parent
        if (parent != null) {
            ExpressionValue<?> val;
            if (!(val = parent.getValue(key)).isNil())
                return (ExpressionValue<V>) val;
        }

        // return nil
        return (ExpressionValue<V>) ExpressionValue.NIL;
    }

    public Context setValue(ExpressionValue<?> key, ExpressionValue<?> value) {
        values.put(key, value);
        return this;
    }

}
