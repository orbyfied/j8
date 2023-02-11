package net.orbyfied.j8.command.exception;

import net.orbyfied.j8.command.Node;
import net.orbyfied.j8.command.text.Text;

public class NodeExecutionException extends CommandExecutionException {

    protected final Node node;

    public NodeExecutionException(Node rootCommand, Node node, String message) {
        super(rootCommand, message);
        this.node = node;
    }

    public NodeExecutionException(Node rootCommand, Node node, Throwable e) {
        super(rootCommand, e);
        this.node = node;
    }

    public NodeExecutionException(Node rootCommand, Node node, String msg, Throwable e) {
        super(rootCommand, msg, e);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String getFormattedPrefix() {
        return super.getFormattedPrefix() + Text.GRAY + " @ " + node.getName();
    }

    @Override
    public String getErrorName() {
        return "Node Execution";
    }

}
