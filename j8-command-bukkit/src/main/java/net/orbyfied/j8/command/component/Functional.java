package net.orbyfied.j8.command.component;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.NodeComponent;
import net.orbyfied.j8.util.StringReader;

public interface Functional extends NodeComponent {

    void walked(Context ctx, StringReader reader);

    void execute(Context ctx);

}
