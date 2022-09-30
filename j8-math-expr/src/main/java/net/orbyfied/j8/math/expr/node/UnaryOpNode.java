package net.orbyfied.j8.math.expr.node;

import net.orbyfied.j8.math.expr.Context;
import net.orbyfied.j8.math.expr.ExpressionNode;
import net.orbyfied.j8.math.expr.ExpressionValue;
import net.orbyfied.j8.math.expr.Operator;
import net.orbyfied.j8.math.expr.error.ExprInterpreterException;

public class UnaryOpNode extends ExpressionNode {
    public UnaryOpNode(Operator op, ExpressionNode node) {
        super(Type.UNARY_OP);
        this.op = op;
        this.node = node;
    }

    public ExpressionNode getNode() {
        return node;
    }

    public Operator getOp() {
        return op;
    }

    ExpressionNode node;
    Operator op;

    @Override
    public ExpressionValue<?> evaluate(Context ctx) {
        ExpressionValue<?> val = node.evaluate(ctx);
        if (val.isNil())
            throw new ExprInterpreterException("got a nil operand")
            .located(getLocation());
        double l = val.getValueAs();
        double o = 0;
        switch (op) {
            case MINUS -> o = -l;
            default -> {
                throw new ExprInterpreterException("operator not implemented: " + op.name())
                .located(getLocation());
            }
        };

        return new ExpressionValue<>(ExpressionValue.Type.NUMBER, o);
    }

    @Override
    protected String getDataAsString() {
        return op.getString() + "(" + node + ")";
    }

}
