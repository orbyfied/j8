package net.orbyfied.j8.util.math.expr.node;

import net.orbyfied.j8.util.math.expr.Context;
import net.orbyfied.j8.util.math.expr.ExpressionNode;
import net.orbyfied.j8.util.math.expr.ExpressionValue;

public class ConstantNode extends ExpressionNode {
    public ConstantNode(ExpressionValue<?> value) {
        super(Type.CONSTANT);
        this.value = value;
    }

    ExpressionValue<?> value;

    @Override
    public ExpressionValue<?> evaluate(Context ctx) {
        return value;
    }

    @Override
    protected String getDataAsString() {
        return value.toString();
    }
}
