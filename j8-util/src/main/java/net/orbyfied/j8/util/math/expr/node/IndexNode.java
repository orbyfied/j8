package net.orbyfied.j8.util.math.expr.node;

import net.orbyfied.j8.util.math.expr.Context;
import net.orbyfied.j8.util.math.expr.ExpressionNode;
import net.orbyfied.j8.util.math.expr.ExpressionValue;

import java.util.HashMap;

public class IndexNode extends ExpressionNode {
    public IndexNode(ExpressionNode src, ExpressionNode key) {
        super(Type.INDEX);
        this.src   = src;
        this.index = key;
    }

    ExpressionNode src;
    ExpressionNode index;

    @Override
    public ExpressionValue<?> evaluate(Context context) {
        ExpressionNode src = this.src;
        ExpressionValue<?> val;
        if (src == null)
            val = context.getValue(index.evaluate(context));
        else
            val = (ExpressionValue<?>) src.evaluate(context)
                    .checkType(ExpressionValue.Type.TABLE)
                    .getValueAs(HashMap.class)
                    .get(index.evaluate(context));
        return val;
    }

    @Override
    protected String getDataAsString() {
        return "" + src + "[" + index + "]";
    }
}
