package net.orbyfied.j8.command.component;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.NodeComponent;
import net.orbyfied.j8.util.StringReader;

public interface Selecting extends NodeComponent {

    boolean selects(Context ctx, StringReader reader);

}
