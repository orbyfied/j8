package net.orbyfied.j8.math.expr.node;

import net.orbyfied.j8.math.expr.Context;
import net.orbyfied.j8.math.expr.ExpressionNode;
import net.orbyfied.j8.math.expr.ExpressionValue;
import net.orbyfied.j8.math.expr.error.ExprInterpreterException;

public class AssignNode extends ExpressionNode {

    ExpressionNode source;
    ExpressionNode index;
    ExpressionNode value;

    public AssignNode(ExpressionNode src, ExpressionNode index, ExpressionNode value) {
        super(Type.ASSIGN);
        this.source = src;
        this.index  = index;
        this.value  = value;
    }

    @Override
    public ExpressionValue<?> evaluate(Context context) {
        // evaluate value
        ExpressionValue<?> val = value.evaluate(context);

        // evaluate index
        ExpressionNode index = this.index;
        ExpressionValue<?> idx;
        if (index == null)
            idx = context;
        else
            idx = index.evaluate(context);

        // decide source
        if (source == null)
            throw new ExprInterpreterException("attempt to index a nil value")
                    .located(getLocation());
        ExpressionValue<?> src = source.evaluate(context);
        if (src.isNil())
            throw new ExprInterpreterException("attempt to index a nil value")
            .located(getLocation());

        // get destination
        try {
            src.structAssign(idx, val);
        } catch (ExprInterpreterException e) {
            throw e.located(getLocation());
        }

        // return value
        return val;
    }

    @Override
    protected String getDataAsString() {
        return "(" + source + ")[" + index + "] = (" + value + ")";
    }
}
