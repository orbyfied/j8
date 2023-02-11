package net.orbyfied.j8.command.exception;

import net.orbyfied.j8.command.Node;

public class CommandHaltException extends CommandException {

    boolean success = false;

    public CommandHaltException(Node rootCommand, String message) {
        super(rootCommand, message);
    }

    public CommandHaltException(Node rootCommand, Throwable e) {
        super(rootCommand, e);
    }

    public CommandHaltException(Node rootCommand, String msg, Throwable e) {
        super(rootCommand, msg, e);
    }

    @Override
    public boolean isSevere() {
        return false;
    }

    public boolean isSuccessful() {
        return success;
    }

    public CommandHaltException setSuccessful(boolean success) {
        this.success = success;
        return this;
    }

}
