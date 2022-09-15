package net.orbyfied.j8.util.math.expr;

import java.util.Arrays;
import java.util.List;

public interface ExpressionFunction<T> {

    class TypedWrappedFunction implements ExpressionFunction<ExpressionNode> {
        ExpressionFunction<ExpressionValue<?>> func;
        List<String> types;

        public TypedWrappedFunction(ExpressionFunction<ExpressionValue<?>> func, List<String> types) {
            this.func = func;
            this.types = types;
        }

        @Override
        public ExpressionValue<?> call(Context ctx, ExpressionNode[] values) {
            if (types == null || types.isEmpty()) {
                // create values array
                ExpressionValue<?>[] args = new ExpressionValue[values.length];
                for (int i = 0; i < values.length; i++)
                    args[i] = values[i].evaluate(ctx);

                // call function
                return func.call(ctx.child(true), args);
            } else {
                // create values
                ExpressionValue<?>[] args = new ExpressionValue[values.length];
                for (int i = 0; i < values.length; i++) {
                    // switch type
                    final String type = types.get(i);
                    ExpressionNode node = values[i];
                    ExpressionValue<?> val = null;
                    if (type != null) {
                        switch (type) {
                            case "expression" -> val = new ExpressionValue<>(ExpressionValue.Type.USER, node);
                            default -> val = node.evaluate(ctx);
                        }
                    } else {
                        val = node.evaluate(ctx);
                    }

                    // put value
                    args[i] = val;
                }

                // call function
                return func.call(ctx.child(true), args);
            }
        }
    }

    static ExpressionFunction<ExpressionNode> wrap(ExpressionFunction<ExpressionValue<?>> func,
                                                   final List<String> types) {
        return new TypedWrappedFunction(func, types);
    }

    static ExpressionValue<?> make(ExpressionFunction<ExpressionValue<?>> func) {
        return new ExpressionValue<>(ExpressionValue.Type.FUNCTION, wrap(func, null));
    }

    static ExpressionValue<?> make(ExpressionFunction<ExpressionValue<?>> func, List<String> types) {
        return new ExpressionValue<>(ExpressionValue.Type.FUNCTION, wrap(func, types));
    }

    static ExpressionValue<?> make(ExpressionFunction<ExpressionValue<?>> func, String... types) {
        return new ExpressionValue<>(ExpressionValue.Type.FUNCTION, wrap(func, Arrays.asList(types)));
    }

    //////////////////////////////////////

    ExpressionValue<?> call(Context ctx, T[] values);

}
