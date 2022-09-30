package net.orbyfied.j8.math.expr.vm;

import net.orbyfied.j8.math.expr.internal.Natives;

public class StackExpressionNativeVM extends ExpressionVM<byte[]> {

    public static void main(String[] args) {
        new StackExpressionNativeVM().execute0(null, null);
    }

    static {
        Natives.loadNativeFromResource(StackExpressionNativeVM.class, "j8mathexpr_native");
        System.out.println("EHHEHEHEHEH");
    }

    ////////////////////////////

    @Override
    protected native void execute0(ExecutionEnvironment env, byte[] code);

}
