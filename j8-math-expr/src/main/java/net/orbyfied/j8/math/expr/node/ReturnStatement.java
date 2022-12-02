package net.orbyfied.j8.math.expr.node;

import net.orbyfied.j8.math.expr.Context;
import net.orbyfied.j8.math.expr.ExpressionValue;

public class ReturnStatement extends Statement {
    public ReturnStatement() {
        super(null);
    }

    @Override
    public ExpressionValue<?> evaluate(Context context) {
        return null;
    }
}
