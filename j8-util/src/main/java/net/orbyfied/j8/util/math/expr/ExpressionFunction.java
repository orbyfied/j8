package net.orbyfied.j8.util.math.expr;

import java.util.List;

public interface ExpressionFunction {

    static ExpressionValue<?> make(ExpressionFunction func) {
        return new ExpressionValue<>(ExpressionValue.Type.FUNCTION, func);
    }

    //////////////////////////////////////

    ExpressionValue<?> call(Context ctx, ExpressionValue<?>[] values);

}
