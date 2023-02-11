package net.orbyfied.j8.expr.ast.exec;

import net.orbyfied.j8.expr.ast.ASTNode;
import net.orbyfied.j8.expr.ast.exec.object.EvalTable;
import net.orbyfied.j8.expr.error.ExprInterpreterException;
import net.orbyfied.j8.expr.util.FastStack;

public class ASTEvaluationContext {

    // the global scope
    protected EvalVariableScope globalScope;

    // the scope stack
    protected final FastStack<EvalStackFrame> frameStack = new FastStack<>(10);

    /////////////////////////
    //// OBJECT HEAP
    /////////////////////////

    // the object heap
    protected EvalObject[] objectHeap = new EvalObject[100];
    // the object reference counts
    protected int[] objectRefs = new int[100];
    // the first known free heap index
    protected int firstFreeIndex = 0;

    public void resizeHeap(int size) {
        EvalObject[] oldHeap = objectHeap;
        int[] oldRefs = objectRefs;
        objectHeap = new EvalObject[size];
        objectRefs = new int[size];
        System.arraycopy(oldHeap, 0, objectHeap, 0, oldHeap.length);
        System.arraycopy(oldRefs, 0, objectRefs, 0, oldRefs.length);
    }

    public EvalObject getHeapObject(int ptr) {
        return objectHeap[ptr];
    }

    public void freeHeapObject(int ptr) {
        objectRefs[ptr] = 0;
        objectHeap[ptr] = null;
        if (ptr < firstFreeIndex)
            firstFreeIndex = ptr;
    }

    public EvalObject allocateHeapObject(byte type) {
        // find location to allocate at
        int loc;
        {
            // check for resize
            if (firstFreeIndex >= objectHeap.length) {
                firstFreeIndex = objectHeap.length;
                resizeHeap((int) (objectHeap.length * 1.5));
            } else {
                // check first free index
                if (objectHeap[firstFreeIndex] != null) {
                    // search for first index
                    while (objectHeap[firstFreeIndex++] != null)
                        if (firstFreeIndex >= objectHeap.length)
                            return allocateHeapObject(type); // will resize the heap
                }
            }

            // set to first free index
            loc = firstFreeIndex;
        }

        // create object
        EvalObject object = switch (type) {
            case EvalObject.OBJECT_TYPE_TABLE -> new EvalTable(loc);
            default -> throw new ExprInterpreterException("alloc: invalid object type: " + type);
        };

        objectHeap[loc] = object;

        // return object
        return object;
    }

    /////////////////////////
    //// CALLS
    /////////////////////////

    public void invokeWithNodeArgs(ASTNode[] args) {
        // TODO create stack frame

        // TODO push arg values

        // TODO call function
    }

    /////////////////////////
    //// VALUE STACK
    /////////////////////////

    // the value array
    EvalValue<?>[] vsArr;
    // the stack pointer
    int vsPtr = -1;

    {
        allocateValueStack(50);
    }

    public void allocateValueStack(int size) {
        EvalValue<?>[] oldArr = vsArr;
        this.vsArr = new EvalValue<?>[size];
        if (vsPtr >= size) vsPtr = size - 1;
        if (oldArr != null)
            System.arraycopy(oldArr, 0, vsArr, 0, oldArr.length);
    }

    public EvalValue<?> getValue(int idx) {
        return vsArr[idx];
    }

    public EvalValue<?> peekValue() {
        if (vsPtr == -1) return null;
        return vsArr[vsPtr];
    }

    public EvalValue<?> popValue() {
        if (vsPtr == -1) return null;
        EvalValue<?> r = vsArr[vsPtr];
        vsArr[vsPtr--] = null;
        return r;
    }

    public void pushValue(EvalValue<?> value) {
        vsPtr++;
        if (vsPtr >= vsArr.length)
            allocateValueStack((int) (vsArr.length * 1.5));
        vsArr[vsPtr] = value;
    }

}
