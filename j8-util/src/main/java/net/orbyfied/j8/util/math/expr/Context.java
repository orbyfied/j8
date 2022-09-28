package net.orbyfied.j8.util.math.expr;

import net.orbyfied.j8.util.math.expr.error.ExprInterpreterException;

import java.util.*;
import java.util.function.Function;

/**
 *
 */
public class Context extends ExpressionValue<HashMap<?, ?>> {

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

    @Override
    public ExpressionValue<HashMap<?, ?>> structIndex(ExpressionValue<?> key) {
        return getValueStrict(key);
    }

    @Override
    public void structAssign(ExpressionValue<?> key, ExpressionValue<?> value) {
        setValueStrict(key, value);
    }

    protected Context(Context parent, Context global) {
        super(Type.TABLE, new HashMap<>());
        this.parent = parent;
        this.global = global;
        this.values = getValueAs();
    }

    /**
     * If this context is local.
     */
    boolean isLocal;

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
    Map<ExpressionValue<?>, ExpressionValue<?>> values;

    /**
     * The call stack.
     */
    public Stack<ExpressionNode> callStack = new Stack<>();

    public Context getGlobal() {
        return global;
    }

    public Context getParent() {
        return parent;
    }

    public Context child() {
        return new Context(this, this.global);
    }

    public Context child(boolean isLocal) {
        Context ctx = new Context(this, this.global);
        ctx.isLocal = isLocal;
        return ctx;
    }

    /* ------- Values ------- */

    public Map<ExpressionValue<?>, ExpressionValue<?>> getValues() {
        return values;
    }

    public boolean containsValueStrict(ExpressionValue<?> key) {
        // inquire local map
        if (values.containsKey(key))
            return true;
        // inquire parent
        if (parent != null && !parent.isLocal) {
            if (parent.containsValueStrict(key))
                return true;
        }
        // inquire global
        if (global != null && global != this) {
            return global.containsValueStrict(key);
        }

        // return nil
        return false;
    }

    public <V> ExpressionValue<V> getValue(Object obj) {
        return getValueStrict(ExpressionValue.of(obj));
    }

    @SuppressWarnings("unchecked")
    public <V> ExpressionValue<V> getValueStrict(ExpressionValue<?> key) {
        // inquire local map
        if (values.containsKey(key))
            return (ExpressionValue<V>) values.get(key);
        // inquire parent
        if (parent != null && !parent.isLocal) {
            ExpressionValue<?> val;
            if (!(val = parent.getValueStrict(key)).isNil())
                return (ExpressionValue<V>) val;
        }
        // inquire global
        if (global != null && global != this) {
            ExpressionValue<?> val;
            if (!(val = global.getValueStrict(key)).isNil())
                return (ExpressionValue<V>) val;
        }

//        System.out.println("returning nil for " + key + " from " + this);

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

    private static ExpressionValue<?> makeDD(Function<Double, Double> func) {
        return ExpressionFunction.make((ctx, args) -> {
            if (args.length < 1)
                throw new ExprInterpreterException("expected double argument");
            return ExpressionValue.ofDouble(func.apply(args[0]
                    .checkType(ExpressionValue.Type.NUMBER)
                    .getValueAs()));
        });
    }

    public static Context openLibs(Context context) {
        /* --------- Lang ----------- */

        context.tableSet("eval", ExpressionFunction.make(((ctx, values) -> {
            return values[0].getValueAs(ExpressionNode.class).evaluate(ctx);
        }), "expression"));

        context.tableSet("expr", ExpressionFunction.make(((ctx, values) -> {
            return values[0];
        }), "expression"));

        context.tableSet("to_string", ExpressionFunction.make((ctx, values) -> {
            return new ExpressionValue<>(Type.STRING, values[0].toString());
        }));

        /* --------- Math ----------- */

        context.tableSet("PI", Math.PI);
        context.tableSet("pi", Math.PI);
        context.tableSet("e",  Math.E);

        ExpressionValue<?> tMath = ExpressionValue.newTable();

        context.tableSet("avg", ExpressionFunction.make((ctx, args) -> {
            double n = 0;
            int l = args.length;
            for (int i = 0; i < l; i++)
                n += args[i]
                        .checkType(ExpressionValue.Type.NUMBER)
                        .getValueAs(Double.class);
            return ExpressionValue.ofDouble(n / l);
        }));

        context.tableSet("sqrt", makeDD(Math::sqrt));
        context.tableSet("sin",  makeDD(Math::sin));
        context.tableSet("cos",  makeDD(Math::cos));
        context.tableSet("tan",  makeDD(Math::tan));

        context.setValue("math", tMath);

        /* ------ Return ------- */
        return context;
    }

    @Override
    public String toString() {
        return "(" + (parent != null ? "->" : "") + "Context)";
    }

}
