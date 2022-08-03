package net.orbyfied.j8.command.component;

import net.orbyfied.j8.command.AbstractNodeComponent;
import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.Node;
import net.orbyfied.j8.command.parameter.Flag;
import net.orbyfied.j8.command.parameter.ParameterType;
import net.orbyfied.j8.util.StringReader;

import java.util.*;

/**
 * A node component which grants the node
 * the ability to push flags to the context.
 * @see Flag
 * @see Context#pushFlag(Flag)
 */
public class Flags
        extends AbstractNodeComponent
        implements Functional {

    public Flags(Node node) {
        super(node);
    }

    // the flags to push
    List<Flag<?>>              flags = new ArrayList<>();
    Map<String, Flag<?>> flagsByName = new HashMap<>();

    /* Getters and Setters */

    public List<Flag<?>> getFlags() {
        return Collections.unmodifiableList(flags);
    }

    public Flag<?> getFlag(String name) {
        return flagsByName.get(name);
    }

    public Flags removeFlag(Flag<?> flag) {
        flags.remove(flag);
        flagsByName.remove(flag.getName());
        return this;
    }

    public Flags removeFlag(String name) {
        return removeFlag(getFlag(name));
    }

    public Flags addFlag(Flag<?> flag) {
        flags.add(flag);
        flagsByName.put(flag.getName(), flag);
        return this;
    }

    public Flags addFlag(String name, Character ch, ParameterType<?> type, boolean isSwitch) {
        return addFlag(new Flag<>(this, name, ch, type, isSwitch));
    }

    /* Functional */

    @Override
    public void walked(Context ctx, StringReader reader) {
        // push flags
        for (Flag<?> fl : flags)
            ctx.pushFlag(fl);
    }

    @Override
    public void execute(Context ctx) { }

}
