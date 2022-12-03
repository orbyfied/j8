package net.orbyfied.j8.expr.ast;

import net.orbyfied.j8.expr.ast.exec.ASTEvaluationContext;

public class LocalScopeNode extends ASTNode {

    public LocalScopeNode() {
        super(ASTNodeType.LOCAL_SCOPE);
    }

    @Override
    public void evaluate(ASTEvaluationContext ctx) {

    }

}
