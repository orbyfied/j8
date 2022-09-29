package net.orbyfied.j8.util.math.expr.vm;

import net.orbyfied.j8.util.Natives;

public class StackExpressionNativeVM extends ExpressionVM<byte[]> {

    static {
        Natives.loadNativeFromResource(StackExpressionNativeVM.class, "j8util_native", "1.0.0", false);
    }

    ////////////////////////////

    @Override
    protected native void execute0(ExecutionEnvironment env, byte[] code);

}
