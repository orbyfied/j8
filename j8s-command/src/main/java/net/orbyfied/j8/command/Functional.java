package net.orbyfied.j8.command;

import net.orbyfied.j8.util.StringReader;

public interface Functional extends NodeComponent {

    void walked(Context ctx, StringReader reader);

    void execute(Context ctx);

}
