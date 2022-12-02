package net.orbyfied.j8.expr.ast;

import net.orbyfied.j8.expr.ast.exec.ASTEvaluationContext;
import net.orbyfied.j8.expr.parser.Operator;

public class UnaryOpNode extends ASTNode {
    public UnaryOpNode(Operator op, ASTNode val) {
        super(ASTNodeType.UNARY_OP);
        this.operator = op;
        this.value    = val;
    }

    // the operator
    Operator operator;
    // the value to do it on
    ASTNode value;

    public Operator getOperator() {
        return operator;
    }

    public UnaryOpNode setOperator(Operator operator) {
        this.operator = operator;
        return this;
    }

    public ASTNode getValue() {
        return value;
    }

    public UnaryOpNode setValue(ASTNode value) {
        this.value = value;
        return this;
    }

    @Override
    public void evaluate(ASTEvaluationContext ctx) {
        value.evaluate(ctx);
        operator.evaluate(ctx);
    }

    @Override
    public String getDataString() {
        return "(" + operator.getSymbol() + value.toString() + ")";
    }

}
