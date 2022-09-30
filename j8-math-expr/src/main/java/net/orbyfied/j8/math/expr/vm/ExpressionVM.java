package net.orbyfied.j8.math.expr.vm;

public abstract class ExpressionVM<R> {

    /**
     * Execute the provided code.
     * @param env The execution environment.
     * @param code The code to run.
     */
    protected abstract void execute0(ExecutionEnvironment env, R code);

}
