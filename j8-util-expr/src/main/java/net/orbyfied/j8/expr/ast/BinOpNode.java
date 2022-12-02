package net.orbyfied.j8.expr.ast;

import net.orbyfied.j8.expr.ast.exec.ASTEvaluationContext;
import net.orbyfied.j8.expr.parser.Operator;

public class BinOpNode extends ASTNode {

    public BinOpNode(Operator operator, ASTNode left, ASTNode right) {
        super(ASTNodeType.BIN_OP);
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    // left and right nodes
    protected ASTNode left;
    protected ASTNode right;
    // the operator
    protected Operator operator;

    public BinOpNode setLeft(ASTNode left) {
        this.left = left;
        return this;
    }

    public BinOpNode setRight(ASTNode right) {
        this.right = right;
        return this;
    }

    public BinOpNode setOperator(Operator operator) {
        this.operator = operator;
        return this;
    }

    public ASTNode getLeft() {
        return left;
    }

    public ASTNode getRight() {
        return right;
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public void evaluate(ASTEvaluationContext ctx) {
        left.evaluate(ctx);
        right.evaluate(ctx);
        operator.evaluate(ctx);
    }

    @Override
    public String getDataString() {
        return "(" + left + " " + operator.getSymbol() + " " + right + ")";
    }

}
