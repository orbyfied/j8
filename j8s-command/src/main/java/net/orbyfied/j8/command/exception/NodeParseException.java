package net.orbyfied.j8.command.exception;

import net.md_5.bungee.api.ChatColor;
import net.orbyfied.j8.command.ErrorLocation;
import net.orbyfied.j8.command.Node;

public class NodeParseException extends CommandParseException {

    protected final Node node;

    public NodeParseException(Node rootCommand, Node node, ErrorLocation loc, String message) {
        super(rootCommand, loc, message);
        this.node = node;
    }

    public NodeParseException(Node rootCommand, Node node, ErrorLocation loc, Exception e) {
        super(rootCommand, loc, e);
        this.node = node;
    }

    public NodeParseException(Node rootCommand, Node node, ErrorLocation loc, String msg, Exception e) {
        super(rootCommand, loc, msg, e);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String getErrorName() {
        return "Node Parsing";
    }

    @Override
    public String getFormattedPrefix() {
        return super.getFormattedPrefix() + ChatColor.GRAY + " @ " + node.getName();
    }

}
