#include "jni.h"
#include <iostream>

extern "C" {
/*
 * Class:     net_orbyfied_j8_math_expr_vm_StackExpressionNativeVM
 * Method:    execute0
 * Signature: (Lnet/orbyfied/j8/math/expr/vm/ExecutionEnvironment;[B)V
 */
JNIEXPORT void JNICALL Java_net_orbyfied_j8_math_expr_vm_StackExpressionNativeVM_execute0
  (JNIEnv *, jobject, jobject, jbyteArray) {
    std::cout << "Hello World!" << std::endl;
}

}
