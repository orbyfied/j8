package net.orbyfied.j8.util.math.expr.node;

import net.orbyfied.j8.util.math.expr.Context;
import net.orbyfied.j8.util.math.expr.ExpressionNode;
import net.orbyfied.j8.util.math.expr.ExpressionValue;
import net.orbyfied.j8.util.math.expr.error.ExprInterpreterException;
import net.orbyfied.j8.util.math.expr.error.LocatedException;

public class AssignNode extends ExpressionNode {

    ExpressionNode source;
    ExpressionNode index;
    ExpressionNode value;

    public AssignNode(ExpressionNode src, ExpressionNode index, ExpressionNode value) {
        super(Type.ASSIGN);
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
        ExpressionValue<?> src;
        if (source == null)
            src = context;
        else
            src = source.evaluate(context);

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
