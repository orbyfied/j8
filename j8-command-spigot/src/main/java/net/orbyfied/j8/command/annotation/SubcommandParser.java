package net.orbyfied.j8.command.annotation;

import net.orbyfied.j8.command.*;
import net.orbyfied.j8.command.component.Flags;
import net.orbyfied.j8.command.component.Primary;
import net.orbyfied.j8.command.argument.Flag;
import net.orbyfied.j8.command.argument.Argument;
import net.orbyfied.j8.command.argument.ArgumentType;
import net.orbyfied.j8.command.argument.TypeIdentifier;
import net.orbyfied.j8.command.impl.CommandNodeExecutor;
import net.orbyfied.j8.registry.Identifier;
import net.orbyfied.j8.util.StringReader;

import java.util.function.Consumer;

public class SubcommandParser {

    /**
     * The command engine.
     */
    protected final CommandManager engine;

    /**
     * The root command node.
     */
    protected final Node root;

    /**
     * The raw string to parse.
     */
    protected final String raw;

    /**
     * The base annotation processor.
     */
    protected final BaseAnnotationProcessor bap;

    public SubcommandParser(BaseAnnotationProcessor bap,
                            CommandManager engine,
                            Node root,
                            String raw) {
        this.bap    = bap;
        this.engine = engine;
        this.root   = root;
        this.raw    = raw;
    }

    public CommandManager getEngine() {
        return engine;
    }

    public Node getRoot() {
        return root;
    }

    public String getRaw() {
        return raw;
    }

    /**
     * Parses the descriptor string into
     * an actual node tree, returning the last executable node.
     * @return The last executable node.
     */
    public Node parse() {
        // create string reader
        StringReader reader = new StringReader(raw, 0);

        // iterate
        Node current = root;
        Node last    = root; // the last executable node
        while (reader.current() != StringReader.DONE) {
            char c1;

            // parse executable (subcommand)
            if ((c1 = reader.current()) != '[' && c1 != '<' && c1 != '(') {
                // collect name
                String component = reader.collect(c -> c != ' ', 1);

                // create and set node
                current = current.getOrCreateChild(component,
                        parent -> new Node(component, parent, parent.root())
                                .executes((CommandNodeExecutor) null));

                // store state
                last = current;
            } else if (c1 == '(') { // parse flag declaration
                // skip (
                reader.next();
                if (reader.current() == StringReader.DONE)
                    throw new AnnotationProcessingException("Unexpected EOF while parsing flag @ idx: " + reader.index());

                // parse type
                TypeIdentifier tid = TypeIdentifier.of(reader.collect(c2 -> c2 != ' '));
                ArgumentType<?> type = engine.getTypeResolver().compile(tid);

                if (reader.current() != ' ')
                    throw new AnnotationProcessingException("Expected ' ' to continue to type declaration @ idx: " + reader.index());

                reader.next();

                // get name
                String name = reader.collect(c -> c != '/' && c != ' ' && c != ')');
                // get character
                Character c = null;
                if (reader.current() == '/') {
                    c = reader.next();
                    reader.next();
                }

                // parse if switch
                boolean isSwitch = false;
                if (reader.current() == ' ') {
                    if (reader.next() == '/') {
                        isSwitch = true;
                        reader.next();
                    }
                }

                // close
                if (reader.current() != ')')
                    throw new AnnotationProcessingException("Expected ')' to close type and flag declaration @ idx: " + reader.index());

                // create and add flag
                Flags flags = last.component(Flags.class, Flags::new);
                Flag<?> flag = new Flag<>(flags, name, c, type, isSwitch);
                flags.addFlag(flag);


                // go to next character
                reader.next();
            } else { // parse parameter
                // check if it is required
                boolean isReq = reader.current() == '<';
                // TODO: required handling

                // skip to next character and check for EOF
                reader.next();
                if (reader.current() == StringReader.DONE)
                    throw new AnnotationProcessingException("Unexpected EOF while parsing parameter @ idx: " + reader.index());

                // collect type and name
                String type = reader.collect(c -> c != ' ', 1);
                String name = reader.collect(c -> c != '>' && c != ']', 1);

                // get or create node
                Node paramNode = current.getChild(name);
                if (paramNode == null) {
                    // parse type into type identifier and resolve
                    TypeIdentifier tid = TypeIdentifier.of(type);
                    ArgumentType<?> pt = engine.getTypeResolver().compile(tid);

                    // create parameter id
                    Identifier pid = new Identifier(null, name);

                    // create node
                    paramNode = current.getOrCreateChild(name,
                            parent -> new Node(name, parent, parent.root())
                                    .argument(pt).getComponent(Argument.class)
                                    .setIdentifier(pid)
                                    .getNode());
                }

                // skip to next character
                reader.next();

                // store state
                current = paramNode;
            }
        }

        // make sure to always set executable
        if (current.getComponentOf(Primary.class) == null)
            current.executes((CommandNodeExecutor) null);
        if (last.getComponentOf(Primary.class) == null)
            last.executes((CommandNodeExecutor) null);

        // return
        return last;
    }

}
