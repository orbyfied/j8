package net.orbyfied.j8.command;

import net.orbyfied.j8.util.StringReader;

public interface Selecting extends NodeComponent {

    boolean selects(Context ctx, StringReader reader);

}