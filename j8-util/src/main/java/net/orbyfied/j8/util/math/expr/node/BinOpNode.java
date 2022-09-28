package net.orbyfied.j8.util.math.expr.node;

import net.orbyfied.j8.util.math.expr.Context;
import net.orbyfied.j8.util.math.expr.ExpressionNode;
import net.orbyfied.j8.util.math.expr.ExpressionValue;
import net.orbyfied.j8.util.math.expr.Operator;
import net.orbyfied.j8.util.math.expr.error.ExprInterpreterException;

public class BinOpNode extends ExpressionNode {
    public BinOpNode(Operator op, ExpressionNode left, ExpressionNode right) {
        super(Type.BIN_OP);
        this.op = op;
        this.left = left;
        this.right = right;
    }

    ExpressionNode left;
    ExpressionNode right;
    Operator op;

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }

    public Operator getOp() {
        return op;
    }

    @Override
    public ExpressionValue<?> evaluate(Context ctx) {
        double l = left.evaluate(ctx).checkNonNil().getValueAs();
        double r = right.evaluate(ctx).checkNonNil().getValueAs();
        double o = 0;
        switch (op) {
            case PLUS     -> o = l + r;
            case MINUS    -> o = l - r;
            case MULTIPLY -> o = l * r;
            case DIVIDE   -> o = l / r;
            case POW      -> o = Math.pow(l, r);
            default -> { throw new ExprInterpreterException("operator not implemented: " + op.name()); }
        };

        return new ExpressionValue<>(ExpressionValue.Type.NUMBER, o);
    }

    @Override
    protected String getDataAsString() {
        return "(" + left + ")" + op.getString() + "(" + right + ")";
    }

}
