package net.orbyfied.j8.command.component;

import net.orbyfied.j8.command.AbstractNodeComponent;
import net.orbyfied.j8.command.Node;

public class Properties extends AbstractNodeComponent {

    protected String description;

    protected String label;

    protected String usage;

    public Properties(Node node) {
        super(node);
    }

    public String description() {
        return description;
    }

    public Properties description(String str) {
        this.description = str;
        return this;
    }

    public String label() {
        return label;
    }

    public Properties label(String str) {
        this.label = str;
        return this;
    }

    public String usage() {
        return usage;
    }

    public Properties usage(String str) {
        this.usage = str;
        return this;
    }

}
