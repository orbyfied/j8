package net.orbyfied.j8.expr.ast;

import net.orbyfied.j8.expr.StringLocation;
import net.orbyfied.j8.expr.ast.exec.ASTEvaluationContext;

public abstract class ASTNode {

    // the location of this node
    StringLocation loc;

    // the type of node
    final ASTNodeType type;

    public ASTNode(ASTNodeType type) {
        this.type = type;
    }

    /**
     * Get the type of this AST node.
     * @return The node type.
     */
    public ASTNodeType getType() {
        return type;
    }

    /**
     * Evaluates this node. This should cause the
     * child nodes to be evaluated and like that it
     * will go on. This is not meant to be fast but
     * rather a way to quickly check functionality
     * before implementing it into the real VM.
     * @param ctx The evaluation context.
     */
    public abstract void evaluate(ASTEvaluationContext ctx);

    public ASTNode located(StringLocation loc) {
        this.loc = loc;
        return this;
    }

    public StringLocation getLocation() {
        return loc;
    }

    // Representation

    public String getDataString() {
        return null;
    }

    @Override
    public String toString() {
        String ds = getDataString();
        return getClass().getSimpleName() + (ds != null ? ds : "");
    }

}
