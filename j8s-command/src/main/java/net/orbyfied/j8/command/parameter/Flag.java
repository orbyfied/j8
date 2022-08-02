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
     * Default value.
     */
    T def;

    /**
     * If this flag is a switch.
     */
    boolean isSwitch;

    public Flag(
            Selecting owner,
            String name,
            Character ch,
            ParameterType<T> type,

            boolean isSwitch
    ) {
        this.owner = owner;
        this.name  = name;
        this.ch    = ch;
        this.type  = type;

        this.isSwitch    = isSwitch;
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

    public boolean isSwitch() {
        return isSwitch;
    }

    public Flag<T> setDefault(T def) {
        this.def = def;
        return this;
    }

    public T getDefault() {
        return def;
    }

}
