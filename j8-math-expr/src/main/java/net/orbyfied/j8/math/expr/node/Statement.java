package net.orbyfied.j8.math.expr.node;

import net.orbyfied.j8.math.expr.ExpressionNode;

public abstract class Statement extends ExpressionNode {
    public Statement(Type type) {
        super(type);
    }
}
