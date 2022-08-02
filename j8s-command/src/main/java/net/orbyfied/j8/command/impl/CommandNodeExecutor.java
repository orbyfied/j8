package net.orbyfied.j8.command.impl;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.Node;

public interface CommandNodeExecutor {

    void execute(Context ctx, Node cmd);

}
