package net.orbyfied.j8.command.exception;

import net.md_5.bungee.api.ChatColor;
import net.orbyfied.j8.command.Node;

public class CommandException extends RuntimeException {

    protected final Node rootCommand;

    public CommandException(Node rootCommand, String message) {
        super(message);
        this.rootCommand = rootCommand;
    }

    public CommandException(Node rootCommand, Throwable e) {
        super(e);
        this.rootCommand = rootCommand;
    }

    public CommandException(Node rootCommand, String msg, Throwable e) {
        super(msg, e);
        this.rootCommand = rootCommand;
    }

    /**
     * Determines if it should be printed
     * to the console and handled like a
     * real, severe error.
     * @return True/false.
     */
    public boolean isSevere() {
        return true;
    }

    public Node getRootCommand() {
        return rootCommand;
    }

    public String getErrorName() {
        return getClass().getSimpleName();
    }

    public String getFormattedPrefix() {
        boolean isWarning = this instanceof Warning;
        ChatColor c = ChatColor.RED;
        if (isWarning)
            c = ChatColor.GOLD;
        return c + (isWarning ? "⚠" : ChatColor.BOLD + "✖") + " " + c + getErrorName() +
                (getCause() != null ? " (" + getCause().getClass().getSimpleName() + ")" : "")
                + ChatColor.DARK_GRAY + " in command(" + rootCommand.getName() + ")";
    }

    public String getFormattedSuffix() {
        return ChatColor.YELLOW + (getMessage() != null ? ": " + getMessage() : "") +
                (getCause() != null ? ": " + getCause().getMessage() : "");
    }

    public String getFormattedString() {
        return getFormattedPrefix() + getFormattedSuffix();
    }

}
