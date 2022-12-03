package net.orbyfied.j8.expr.parser;

import net.orbyfied.j8.expr.ast.exec.ASTEvaluationContext;
import net.orbyfied.j8.expr.ast.exec.EvalFunction;
import net.orbyfied.j8.expr.ast.exec.EvalValue;

import java.util.function.Consumer;

public enum Operator {

    // TODO: operator overloading

    // default operators
    ADD("+", "add", ctx -> {
        EvalValue<?> av = ctx.popValue();
        EvalValue<?> bv = ctx.popValue();
        double a = av.requireType(EvalValue.TYPE_NUMBER).getValueAs();
        double b = bv.requireType(EvalValue.TYPE_NUMBER).getValueAs();
        ctx.pushValue(new EvalValue<>(EvalValue.TYPE_NUMBER, (double)(a + b)));
    }),
    SUB("-", "sub", ctx -> {
        double a = ctx.popValue().requireType(EvalValue.TYPE_NUMBER).getValueAs();
        double b = ctx.popValue().requireType(EvalValue.TYPE_NUMBER).getValueAs();
        ctx.pushValue(new EvalValue<>(EvalValue.TYPE_NUMBER, (double)(a - b)));
    }),
    MUL("*", "mul", ctx -> {
        double a = ctx.popValue().requireType(EvalValue.TYPE_NUMBER).getValueAs();
        double b = ctx.popValue().requireType(EvalValue.TYPE_NUMBER).getValueAs();
        ctx.pushValue(new EvalValue<>(EvalValue.TYPE_NUMBER, (double)(a * b)));
    }),
    DIV("/", "div", ctx -> {
        double a = ctx.popValue().requireType(EvalValue.TYPE_NUMBER).getValueAs();
        double b = ctx.popValue().requireType(EvalValue.TYPE_NUMBER).getValueAs();
        ctx.pushValue(new EvalValue<>(EvalValue.TYPE_NUMBER, (double)(a / b)));
    }),
    NEGATE("-", "negate", ctx -> {
        double n = ctx.popValue().requireType(EvalValue.TYPE_NUMBER).getValueAs();
        ctx.pushValue(new EvalValue<>(EvalValue.TYPE_NUMBER, -n));
    });

    /////////////////////////////////////////////////////

    final String symbol;
    final String name;
    final Consumer<ASTEvaluationContext> evaluator;

    Operator(String symbol, String name, Consumer<ASTEvaluationContext> evaluator) {
        this.symbol = symbol;
        this.name   = name;
        this.evaluator = evaluator;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public Consumer<ASTEvaluationContext> getEvaluator() {
        return evaluator;
    }

    @Override
    public String toString() {
        return name;
    }

    public void evaluate(ASTEvaluationContext ctx) {
        evaluator.accept(ctx);
    }

}
