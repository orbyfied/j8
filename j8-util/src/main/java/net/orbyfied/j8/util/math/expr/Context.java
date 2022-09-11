package net.orbyfied.j8.util.math.expr;

import java.util.Arrays;
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

    public static Context newDefaultGlobal() {
        Context ctx = new Context(null, null);
        ctx.global = ctx;
        openLibs(ctx);
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

    public Context getGlobal() {
        return global;
    }

    public Context getParent() {
        return parent;
    }

    public Context child() {
        return new Context(this, this.global);
    }

    /* ------- Values ------- */

    public <V> ExpressionValue<V> getValue(Object obj) {
        return getValueStrict(ExpressionValue.of(obj));
    }

    @SuppressWarnings("unchecked")
    public <V> ExpressionValue<V> getValueStrict(ExpressionValue<?> key) {
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

    public Context setValue(Object key, Object val) {
        return setValueStrict(ExpressionValue.of(key), ExpressionValue.of(val));
    }

    public Context setValueStrict(ExpressionValue<?> key, ExpressionValue<?> value) {
        values.put(key, value);
        return this;
    }

    /////////////////////////////////////

    public static Context openLibs(Context context) {

        /* --------- Math ----------- */

        ExpressionValue<?> tMath = ExpressionValue.newTable();

        tMath.tableSet("avg", ExpressionFunction.make(args -> {
            double n = 0;
            int l = args.length;
            for (int i = 0; i < l; i++)
                n += args[i]
                        .checkType(ExpressionValue.Type.NUMBER)
                        .getValueAs(Double.class);
            return ExpressionValue.ofDouble(n / l);
        }));

        tMath.tableSet("sqrt", ExpressionFunction.make(args -> {
            return ExpressionValue.ofDouble(Math.sqrt(args[0]
                    .checkType(ExpressionValue.Type.NUMBER)
                    .getValueAs(Double.class)));
        }));

        context.setValue("math", tMath);

        /* ------ Return ------- */
        return context;
    }

}
