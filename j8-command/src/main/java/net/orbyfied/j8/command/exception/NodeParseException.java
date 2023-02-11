package net.orbyfied.j8.command.exception;

import net.orbyfied.j8.command.Node;
import net.orbyfied.j8.command.text.Text;

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
        return super.getFormattedPrefix() + Text.GRAY + " @ " + node.getName();
    }

}
