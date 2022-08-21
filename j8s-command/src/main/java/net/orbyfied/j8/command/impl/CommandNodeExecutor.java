package net.orbyfied.j8.command.impl;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.Node;

/**
 * Executor for an executable node.
 */
public interface CommandNodeExecutor {

    /**
     * Called when either walked or executed.
     * @param ctx The context.
     * @param cmd The node.
     */
    void execute(Context ctx, Node cmd);

}
