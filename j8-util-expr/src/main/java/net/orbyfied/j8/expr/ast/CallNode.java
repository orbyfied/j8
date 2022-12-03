package net.orbyfied.j8.expr.ast;

import net.orbyfied.j8.expr.ast.exec.ASTEvaluationContext;

import java.util.Arrays;

public class CallNode extends ASTNode {

    public CallNode(ASTNode func, ASTNode[] args) {
        super(ASTNodeType.CALL);
        this.func = func;
        this.args = args;
    }

    // the function
    ASTNode func;
    // the args
    ASTNode[] args;

    @Override
    public void evaluate(ASTEvaluationContext ctx) {
        // evaluate function
        func.evaluate(ctx);

        // call
        ctx.invokeWithNodeArgs(args);
    }

    @Override
    public Object[] getDebugArgs() {
        return new Object[] { func, args };
    }

}
