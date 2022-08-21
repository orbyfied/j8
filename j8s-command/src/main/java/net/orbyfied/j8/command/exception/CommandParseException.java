package net.orbyfied.j8.command.exception;

import net.md_5.bungee.api.ChatColor;
import net.orbyfied.j8.command.Node;

public class CommandParseException extends CommandException {

    protected ErrorLocation location;

    public CommandParseException(Node rootCommand, ErrorLocation loc, String message) {
        super(rootCommand, message);
        this.location = loc;
    }

    public CommandParseException(Node rootCommand, ErrorLocation loc, Exception e) {
        super(rootCommand, e);
        this.location = loc;
    }

    public CommandParseException(Node rootCommand, ErrorLocation loc, String msg, Exception e) {
        super(rootCommand, msg, e);
        this.location = loc;
    }

    @Override
    public boolean isSevere() {
        return false;
    }

    public ErrorLocation getLocation() {
        return location;
    }

    @Override
    public String getErrorName() {
        return "Command Parsing";
    }

    @Override
    public String getFormattedString() {
        return super.getFormattedPrefix() + ChatColor.WHITE + " at " + location.getLocationString() + getFormattedSuffix();
    }

}
