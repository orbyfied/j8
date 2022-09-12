package net.orbyfied.j8.util.math.expr.node;

import net.orbyfied.j8.util.math.expr.Context;
import net.orbyfied.j8.util.math.expr.ExpressionFunction;
import net.orbyfied.j8.util.math.expr.ExpressionNode;
import net.orbyfied.j8.util.math.expr.ExpressionValue;
import net.orbyfied.j8.util.math.expr.error.ExprInterpreterException;

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
        // evaluate parameters
        ExpressionValue<?>[] paramValues = new ExpressionValue[params.size()];
        for (int i = 0; i < params.size(); i++)
            paramValues[i] = params.get(i).evaluate(context);

        // get function
        ExpressionValue<?> fn = func.evaluate(context);
        if (fn.getType() != ExpressionValue.Type.FUNCTION)
            throw new ExprInterpreterException("attempt to call a " + fn.getType().getName() + " value");

        // evaluate function
        return func.evaluate(context)
                .checkType(ExpressionValue.Type.FUNCTION)
                .getValueAs(ExpressionFunction.class)
                .call(context, paramValues);
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
