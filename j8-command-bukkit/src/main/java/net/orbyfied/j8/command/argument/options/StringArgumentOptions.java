package net.orbyfied.j8.command.argument.options;

public class StringArgumentOptions extends ArgumentOptions {

    /**
     * If it should read past the spaces until the end.
     */
    protected boolean readFar;

    public boolean readFar() {
        return readFar;
    }

    public StringArgumentOptions readFar(boolean b) {
        this.readFar = b;
        return this;
    }

}
