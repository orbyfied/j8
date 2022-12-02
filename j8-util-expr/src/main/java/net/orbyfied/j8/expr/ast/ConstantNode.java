package net.orbyfied.j8.expr.ast;

import net.orbyfied.j8.expr.ast.exec.ASTEvaluationContext;
import net.orbyfied.j8.expr.ast.exec.EvalValue;

public class ConstantNode extends ASTNode {

    public ConstantNode(EvalValue<?> value) {
        super(ASTNodeType.CONSTANT);
        this.value = value;
    }

    // the value to push
    EvalValue<?> value;

    public EvalValue<?> getValue() {
        return value;
    }

    @Override
    public void evaluate(ASTEvaluationContext ctx) {
        ctx.pushValue(value);
    }

    @Override
    public String getDataString() {
        return "=" + value.toString();
    }

}
