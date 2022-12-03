package net.orbyfied.j8.expr.ast;

import net.orbyfied.j8.expr.util.StringLocation;
import net.orbyfied.j8.expr.ast.exec.ASTEvaluationContext;
import net.orbyfied.j8.util.StringUtil;

import java.util.StringJoiner;

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

    public Object[] getDebugArgs() {
        return new Object[0];
    }

    @Override
    public String toString() {
        String ds = getDataString();
        StringJoiner j = new StringJoiner(", ");
        for (Object o : getDebugArgs())
            j.add(StringUtil.toStringDebug(o));
        return type.getName() + "(" + j + ")" + (ds != null ? ":" + ds : "");
    }

}
