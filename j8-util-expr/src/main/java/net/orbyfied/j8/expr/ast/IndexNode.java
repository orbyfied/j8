package net.orbyfied.j8.expr.ast;

import net.orbyfied.j8.expr.ast.exec.ASTEvaluationContext;
import net.orbyfied.j8.expr.ast.exec.EvalObject;
import net.orbyfied.j8.expr.ast.exec.EvalValue;

public class IndexNode extends ASTNode {

    public IndexNode(ASTNode base, ASTNode key) {
        super(ASTNodeType.INDEX);
        this.base = base;
        this.key  = key;
    }

    ASTNode base;
    ASTNode key;

    @Override
    public void evaluate(ASTEvaluationContext ctx) {
        // get base
        EvalValue<?> baseVal;
        if (base != null) {
            base.evaluate(ctx);
            baseVal = ctx.popValue();
        } else baseVal = null;
        // get key
        key.evaluate(ctx);
        EvalValue<?> keyVal = ctx.popValue();

        // TODO if baseVal != null
        // TODO    index table or call index overload function
        // TODO else
        // TODO    index local scope
    }

    @Override
    public Object[] getDebugArgs() {
        return new Object[] { base, key };
    }

}
