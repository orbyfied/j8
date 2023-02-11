package net.orbyfied.j8.expr.util;

import net.orbyfied.j8.expr.ast.exec.EvalValue;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

@SuppressWarnings("unchecked")
public class FastStack<T> {

    // if fast stack successfully loaded
    private static boolean loaded = false;
    // get unsafe
    private static Unsafe unsafe;

    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
        } catch (Exception e) {
            System.err.println("Failed to load FastStack: Unable to get unsafe");
            e.printStackTrace();
        }
    }

    public FastStack() {
        allocate(10);
    }

    public FastStack(int initSize) {
        allocate(initSize);
    }

    // the value array
    T[] arr;
    // the stack pointer
    int ptr;

    public void allocate(int size) {
        T[] oldArr = arr;
        this.arr = (T[]) new Object[size];
        if (ptr >= size) ptr = size - 1;
        if (oldArr != null)
            System.arraycopy(oldArr, 0, arr, 0, oldArr.length);
    }

    public T get(int idx) {
        return arr[idx];
    }

    public T peek() {
        if (ptr == -1) return null;
        return arr[ptr];
    }

    public T pop() {
        if (ptr == -1) return null;
        T r = arr[ptr];
        arr[ptr--] = null;
        return r;
    }

    public T popOr(T e) {
        if (ptr == -1) return e;
        T r = arr[ptr];
        arr[ptr--] = null;
        return r;
    }

    public void push(T value) {
        ptr++;
        if (ptr >= arr.length)
            allocate((int) (arr.length * 1.5));
        arr[ptr] = value;
    }

    public int size() {
        return ptr;
    }

    public int allocated() {
        return arr.length;
    }

}
