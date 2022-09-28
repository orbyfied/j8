package net.orbyfied.j8.util.math.expr.node;

import net.orbyfied.j8.util.math.expr.Context;
import net.orbyfied.j8.util.math.expr.ExpressionNode;
import net.orbyfied.j8.util.math.expr.ExpressionValue;
import net.orbyfied.j8.util.math.expr.error.ExprInterpreterException;

import java.util.HashMap;

public class IndexNode extends ExpressionNode {
    public IndexNode(ExpressionNode src, ExpressionNode key) {
        super(Type.INDEX);
        this.src   = src;
        this.index = key;
    }

    public ExpressionNode src;
    public ExpressionNode index;

    public ExpressionNode getSource() {
        return src;
    }

    public ExpressionNode getIndex() {
        return index;
    }

    @Override
    public ExpressionValue<?> evaluate(Context context) {
        // evaluate source
        ExpressionNode src = this.src;
        ExpressionValue<?> srcVal = src.evaluate(context);
        if (srcVal == null || srcVal.isNil())
            throw new ExprInterpreterException("attempt to index a nil value")
            .located(getLocation());

        // evaluate key
        ExpressionValue<?> indexVal = index.evaluate(context);

        // index
        try {
            return srcVal.structIndex(indexVal);
        } catch (ExprInterpreterException e) {
            throw e.located(getLocation());
        }
    }

    @Override
    protected String getDataAsString() {
        return "(" + src + ")[" + index + "]";
    }
}
