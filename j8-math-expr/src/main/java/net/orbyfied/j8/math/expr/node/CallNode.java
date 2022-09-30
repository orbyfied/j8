package net.orbyfied.j8.math.expr.node;

import net.orbyfied.j8.math.expr.Context;
import net.orbyfied.j8.math.expr.ExpressionFunction;
import net.orbyfied.j8.math.expr.ExpressionNode;
import net.orbyfied.j8.math.expr.ExpressionValue;
import net.orbyfied.j8.math.expr.error.ExprInterpreterException;

import java.util.List;

public class CallNode extends ExpressionNode {
    public CallNode(ExpressionNode func, List<ExpressionNode> params) {
        super(Type.CALL);
        this.func   = func;
        this.params = params;
    }

    ExpressionNode func;
    List<ExpressionNode> params;

    @Override
    public ExpressionValue<?> evaluate(Context context) {
        // get function
        ExpressionValue<?> fn = func.evaluate(context);
        if (fn.getType() != ExpressionValue.Type.FUNCTION)
            throw new ExprInterpreterException("attempt to call a " + fn.getType().getName() + " value")
                    .located(getLocation());

        // evaluate function
        try {
            return func.evaluate(context)
                    .<ExpressionFunction<ExpressionNode>>getValueAs()
                    .call(context, params.toArray(new ExpressionNode[0]));
        } catch (ExprInterpreterException e) {
            if (e.getLocation() == null)
                e.located(getLocation());
            throw e;
        }
    }

    @Override
    protected String getDataAsString() {
        StringBuilder b = new StringBuilder();
        for (ExpressionNode p : params) {
            if (!b.isEmpty())
                b.append(", ");
            b.append(p);
        }
        return "(" + func + ")(" + b + ")";
    }
}
