package net.orbyfied.j8.math.expr.node;

import net.orbyfied.j8.math.expr.Context;
import net.orbyfied.j8.math.expr.ExpressionNode;
import net.orbyfied.j8.math.expr.ExpressionValue;

public class IfNode extends ExpressionNode {

    public IfNode(Type type) {
        super(type);
    }

    // primary if
    ExpressionNode condition;
    ExpressionNode body;

    // else
    ExpressionNode elseBody;

    @Override
    public ExpressionValue<?> evaluate(Context context) {
        // evaluate condition
        ExpressionValue<?> value = condition.evaluate(context);

        // check
        if (value.checkBool()) {
            return body.evaluate(context);
        } else {
            if (elseBody != null)
                return elseBody.evaluate(context);
        }

        return ExpressionValue.NIL;
    }

}
