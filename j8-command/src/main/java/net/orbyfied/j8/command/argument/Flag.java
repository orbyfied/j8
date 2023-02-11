package net.orbyfied.j8.command.argument;

import net.orbyfied.j8.command.component.Flags;

/**
 * Represents a command flag.
 *
 * @param <T> The value type.
 */
public class Flag<T> {

    /**
     * The owner of a flag.
     */
    final Flags owner;

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
    final ArgumentType<T> type;

    /**
     * Default value.
     */
    T def;

    /**
     * If this flag is a switch.
     */
    boolean isSwitch;

    public Flag(
            Flags owner,
            String name,
            Character ch,
            ArgumentType<T> type,

            boolean isSwitch
    ) {
        this.owner = owner;
        this.name  = name;
        this.ch    = ch;
        this.type  = type;

        this.isSwitch    = isSwitch;
    }

    public Flags getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Character getCharacter() {
        return ch;
    }

    public ArgumentType<T> getType() {
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
