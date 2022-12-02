package net.orbyfied.j8.expr.ast.exec;

import net.orbyfied.j8.expr.util.FastStack;

public class ASTEvaluationContext {

    // the value stack
    FastStack<EvalValue<?>> valueStack = new FastStack<>(50);

    // the global scope
    EvalVariableScope globalScope;

    // the scope stack
    FastStack<EvalStackFrame> frameStack = new FastStack<>(10);

    public FastStack<EvalValue<?>> getValueStack() {
        return valueStack;
    }

    public EvalValue<?> popValue() {
        return valueStack.popOr(EvalValue.NIL);
    }

    public void pushValue(EvalValue<?> value) {
        valueStack.push(value);
    }

}
