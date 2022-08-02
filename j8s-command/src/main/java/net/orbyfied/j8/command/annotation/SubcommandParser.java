package net.orbyfied.j8.command.annotation;

import net.orbyfied.j8.command.*;
import net.orbyfied.j8.command.parameter.Flag;
import net.orbyfied.j8.command.parameter.Parameter;
import net.orbyfied.j8.command.parameter.ParameterType;
import net.orbyfied.j8.command.parameter.TypeIdentifier;
import net.orbyfied.j8.registry.Identifier;
import net.orbyfied.j8.util.StringReader;

public class SubcommandParser {

    /**
     * The command engine.
     */
    protected final CommandEngine engine;

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
                            CommandEngine engine,
                            Node root,
                            String raw) {
        this.bap    = bap;
        this.engine = engine;
        this.root   = root;
        this.raw    = raw;
    }

    public CommandEngine getEngine() {
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
            if ((c1 = reader.current()) != '[' && c1 != '<' && c1 != '-') {
                // collect name
                String component = reader.collect(c -> c != ' ', 1);

                // create and set node
                current = current.getOrCreateSubnode(component,
                        parent -> new Node(component, parent, parent.getRoot())
                                .makeExecutable(null));

                // store state
                last = current;
            } else if (c1 == '-') { // parse flag declaration
                if (reader.peek(1) == '-')
                    reader.next();

                // get name
                String name = reader.collect(c -> c != '/' && c != '(');
                // get character
                Character c = null;
                if (reader.current() == '/') {
                    c = reader.next();
                    reader.next();
                }

                if (reader.current() != '(')
                    throw new AnnotationProcessingException("Expected '(' to open type and flag declaration");

                // parse type
                reader.next();
                TypeIdentifier tid = TypeIdentifier.of(reader.collect(c2 -> c2 != ' ' && c2 != ')'));
                ParameterType<?> type = engine.getTypeResolver().compile(tid);

                // parse if switch
                boolean isSwitch = false;
                while (reader.current() == ' ') {
                    switch (reader.next()) {
                        case 's' -> isSwitch = true;
                    }
                    reader.next();
                }

                // close
                if (reader.current() != ')')
                    throw new AnnotationProcessingException("Expected ')' to close type and flag declaration");

                // create and add flag
                Executable sel = last.getComponent(Executable.class);
                Flag<?> flag = new Flag<>(sel, name, c, type, isSwitch);
                sel.getFlags().add(flag);
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
                Node paramNode = current.getSubnode(name);
                if (paramNode == null) {
                    // parse type into type identifier and resolve
                    TypeIdentifier tid = TypeIdentifier.of(type);
                    ParameterType<?> pt = engine.getTypeResolver().compile(tid);

                    // create parameter id
                    Identifier pid = new Identifier(null, name);

                    // create node
                    paramNode = current.getOrCreateSubnode(name,
                            parent -> new Node(name, parent, parent.getRoot())
                                    .makeParameter(pt).getComponent(Parameter.class)
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
        if (current.getComponentOf(Selecting.class) == null)
            current.makeExecutable(null);

        // return
        return last;
    }

}
