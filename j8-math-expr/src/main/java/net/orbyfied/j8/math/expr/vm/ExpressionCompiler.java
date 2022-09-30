package net.orbyfied.j8.math.expr.vm;

import net.orbyfied.j8.math.expr.ExpressionNode;
import net.orbyfied.j8.math.expr.error.ExprCompilerException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

public abstract class ExpressionCompiler<R, B> {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    ///////////////////////////////////////////////////////

    /**
     * Create a new buffer.
     * @return The buffer.
     */
    public abstract B create();

    /**
     * Finishes off and compiles the buffer
     * into the end result.
     * @param buffer The buffer.
     * @return The end result.
     */
    public abstract R finish(B buffer);

    // the node compilers cached
    private final Map<Class<? extends ExpressionNode>, MethodHandle> nodeCompilers = new HashMap<>();

    /**
     * Tries to compile the node into the buffer.
     * It tries to lookup and call a method
     * {@code compile$NodeTypeName}.
     * @param node The node to compile.
     * @param buffer The buffer to compile into.
     */
    public void compile(ExpressionNode node, B buffer) {
        // check if null
        if (node == null)
            return;
        Class<? extends ExpressionNode> klass = node.getClass();

        // try to index cache
        MethodHandle handle;
        if ((handle = nodeCompilers.get(klass)) == null) {
            try {
                // try to lookup handle
                final String name = "compile$" + node.getType().getName();
                handle = lookup.findVirtual(this.getClass(), name,
                        MethodType.methodType(Void.class, new Class[]{
                                klass, buffer.getClass()}));

                // cache handle
                nodeCompilers.put(klass, handle);
            } catch (NoSuchMethodException e) {
                throw new ExprCompilerException("No compiler method for node type " + node.getType().getName());
            } catch (Exception e) {
                throw new ExprCompilerException("Could not find compiler for type " + node.getType().getName(), e);
            }
        }

        try {
            // call compiler method
            handle.invoke(this, node, buffer);
        } catch (Throwable t) {
            if (!(t instanceof ExprCompilerException)) {
                throw new ExprCompilerException("Error while compiling a node of type " + node.getType().getName(), t)
                        .located(node.getLocation());
            }
        }
    }

}
