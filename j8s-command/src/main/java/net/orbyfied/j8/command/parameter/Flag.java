package net.orbyfied.j8.command.parameter;

import net.orbyfied.j8.command.Selecting;

public class Flag<T> {

    /**
     * The owner of a flag.
     */
    final Selecting owner;

    /**
     * The full name of this flag.
     */
    final String name;

    /**
     * The one character identifier.
     * Null means off.
     */
    final Character ch;

    /**
     * The type of this flag.
     */
    final ParameterType<T> type;

    /**
     * The default value of this flag.
     */
    T def;

    /**
     * If this flag is a switch.
     */
    boolean isSwitch;

    /**
     * The value to set if the switch is on.
     */
    T switchValue;

    public Flag(
            Selecting owner,
            String name,
            Character ch,
            ParameterType<T> type,
            T def,

            boolean isSwitch,
            T switchValue
    ) {
        this.owner = owner;
        this.name  = name;
        this.ch    = ch;
        this.type  = type;
        this.def   = def;

        this.isSwitch    = isSwitch;
        this.switchValue = switchValue;
    }

    public Selecting getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Character getCharacter() {
        return ch;
    }

    public ParameterType<T> getType() {
        return type;
    }

    public T getDefault() {
        return def;
    }

    public boolean isSwitch() {
        return isSwitch;
    }

    public T getSwitchValue() {
        return switchValue;
    }

}
