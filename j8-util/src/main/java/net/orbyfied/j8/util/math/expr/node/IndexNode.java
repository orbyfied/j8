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

    public ExpressionNode src;
    public ExpressionNode index;

    @Override
    public ExpressionValue<?> evaluate(Context context) {
        ExpressionNode src = this.src;
        ExpressionValue<?> val;
        ExpressionValue<?> srcVal;
        if (src == null)
            srcVal = context;
        else
            srcVal = src.evaluate(context);
        ExpressionValue<?> indexVal = index.evaluate(context);
        val = srcVal.structIndex(index.evaluate(context));
        return val;
    }

    @Override
    protected String getDataAsString() {
        return "(" + src + ")[" + index + "]";
    }
}
