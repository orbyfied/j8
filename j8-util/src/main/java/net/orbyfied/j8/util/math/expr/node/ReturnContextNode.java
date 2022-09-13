package net.orbyfied.j8.util.math.expr.node;

import net.orbyfied.j8.util.math.expr.Context;
import net.orbyfied.j8.util.math.expr.ExpressionNode;
import net.orbyfied.j8.util.math.expr.ExpressionValue;

public class ReturnContextNode extends ExpressionNode {
    public ReturnContextNode() {
        super(Type.MISC);
    }

    @Override
    public ExpressionValue<?> evaluate(Context context) {
        return context;
    }
}
