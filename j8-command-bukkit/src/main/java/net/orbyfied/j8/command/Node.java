package net.orbyfied.j8.command;

import net.orbyfied.j8.command.argument.ArgumentTypes;
import net.orbyfied.j8.command.component.*;
import net.orbyfied.j8.command.component.Properties;
import net.orbyfied.j8.command.exception.CommandException;
import net.orbyfied.j8.command.exception.CommandParseException;
import net.orbyfied.j8.command.exception.ErrorLocation;
import net.orbyfied.j8.command.exception.NodeParseException;
import net.orbyfied.j8.command.impl.CommandNodeExecutor;
import net.orbyfied.j8.command.argument.Flag;
import net.orbyfied.j8.command.argument.Argument;
import net.orbyfied.j8.command.argument.ArgumentType;
import net.orbyfied.j8.util.ReflectionUtil;
import net.orbyfied.j8.util.StringReader;
import org.bukkit.ChatColor;

import java.io.PrintStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a command node in the
 * command tree. Has components that
 * determine what it is and can do.
 */
public class Node {

    /**
     * The node that stands for unknown sub node.
     * Used in execution to fail and to suggest
     * using the previous node in suggesting.
     */
    static final Node NODE_UNKNOWN = new Node("<unknown>", null, null)
            .executes(null, (ctx, cmd) -> {
                if (!ctx.isSuggesting()) {
                    ctx.fail("Unknown sub-command " + ChatColor.WHITE + ctx.getOption("__fast_node_name").orElse(null));
                }
            });

    ///////////////////////////////////////////////////

    /**
     * The components stored in a linear list.
     */
    protected final ArrayList<NodeComponent> components = new ArrayList<>();

    /**
     * The components mapped by class.
     * Includes all parent classes of a
     * component which are not annotated
     * with {@link NonComponent}
     */
    protected final HashMap<Class<?>, NodeComponent> componentsByClass = new HashMap<>();

    /**
     * The children (subnodes/subcommands) of this node.
     */
    protected final ArrayList<Node> children = new ArrayList<>();

    /**
     * The children (subnodes/subcommands) of this node
     * mapped by name.
     */
    protected final HashMap<String, Node> childrenByName = new HashMap<>();

    /**
     * NOTE: Only subcommands will be stored here, no
     * parameters as they have nothing to be mapped to.
     */
    protected final HashMap<String, Node> fastMappedChildren = new HashMap<>();

    /**
     * The primary name of this node.
     */
    protected final String name;

    /**
     * The aliases of this node.
     */
    protected final List<String> aliases = new ArrayList<>();

    /**
     * The immediate parent of this node.
     */
    protected final Node parent;

    /**
     * The root node of this tree.
     */
    protected Node root;

    /** Constructor. */
    public Node(final String name,
                final Node parent,
                final Node root) {
        this.name   = name;
        this.parent = parent;
        this.root = Objects.requireNonNullElse(root, this);

        // add default suggester
        withComponent(Suggester.defaults(this));
    }

    ////////////////////////////////////////////////
    //// DISPATCH
    ////////////////////////////////////////////////

    boolean executeOrBacktrack(Context context) {
        // execute components
        if (!context.isSuggesting()) {
            Executable executable = getComponent(Executable.class);
            if (executable != null) {
                executable.execute(context);
                return false;
            }
        }

        // backtrack
        return true;
    }

    /**
     * Walks this node in a dispatch chain,
     * this node will handle it's own behaviour
     * and then move on to the next or terminate
     * the chain.
     * @param context The command context.
     * @param last The last node in the chain.
     * @param fReader The string reader.
     * @return If the chain has ended and should be back tracked
     *         further to the last executable.
     */
    public boolean walk(
            Context context,
            Node last,
            StringReader fReader
    ) {
        // get root node
        Node root = context.rootCommand();

        // get suggestions
        SuggestionAccumulator suggestions = context.suggestions();

        // collect whitespace
        while (Character.isWhitespace(fReader.current()))
            fReader.next();
        if (fReader.current() == StringReader.DONE) {
            return executeOrBacktrack(context);
        }

        // branch string reader for parsing
        StringReader reader = fReader.branch();

        // walk components
        Primary primary;
        if ((primary = getComponentOf(Primary.class)) == null)
            throw new CommandException(this, "Invalid node, no primary component set");
        for (NodeComponent component : components) {
            // check for functional
            if (component instanceof Functional func) {
                func.walked(context, reader);
            }
        }

        // advance one more
        reader.next();

        // parse flags
        List<Runnable> flagCompletions = new ArrayList<>();
        {
            if (reader.current() == '-') {
                char c;
                while ((c = reader.current()) != StringReader.DONE && c == '-') {
                    // empty completions
                    flagCompletions.clear();

                    // get start index
                    int sIdx = reader.index();

                    // check for fully qualified flag
                    if ((c = reader.next()) == '-') {
                        // collect name
                        reader.next();
                        String flagName = reader.collect(c1 -> c1 != '=' && c1 != ' ');

                        // get flag
                        Flag<?> flag = context.getFlagByName(flagName);

                        // suggest names
                        if (reader.current() != '=') {
                            if (context.isSuggesting()) {
                                flagCompletions.add(() -> {
                                    suggestions.pushPrefix("--");
                                    for (Flag<?> f : context.getFlags()) {
                                        suggestions.suggest(f.getName());
                                    }
                                    suggestions.popPrefix();
                                });
                            }
                        }

                        // check for foreign
                        if (flag != null) {
                            // get value
                            Object value;
                            if (reader.current() == '=') {
                                reader.next();
                                // collect value
                                try {
                                    value = flag.getType().parse(context, reader);
                                } catch (Exception e) {
                                    throw new NodeParseException(root, this,
                                            new ErrorLocation(reader, sIdx, reader.index()), e);
                                }

                                // suggest value
                                if (context.isSuggesting()) {
                                    flagCompletions.add(() -> {
                                        suggestions.pushPrefix("--" + flagName + "=");
                                        flag.getType().suggest(context, suggestions);
                                        suggestions.popPrefix();
                                    });
                                }
                            } else {
                                // check switch
                                if (!flag.isSwitch())
                                    throw new CommandParseException(root,
                                            new ErrorLocation(reader, sIdx + 1, reader.index()),
                                            "Flag --" + flag.getName() + " is not a switch, but no value was provided.");
                                value = true;
                            }

                            // put value
                            context.flagValues.put(flag, value);
                        } else /* foreign */ {
                            // get value
                            Object value;
                            reader.next();
                            if (reader.current() == '=') {
                                reader.next();
                                // collect value
                                value = ArgumentTypes.STRING.parse(context, reader);
                            } else {
                                value = true;
                            }

                            // put value
                            context.foreignFlagValues.put(flagName, value);
                        }
                    } else /* symbol flags */ {
                        // get character symbols
                        char fg;
                        while ((fg = reader.current()) != StringReader.DONE && fg != ' ') {
                            // get flag
                            Flag<?> flag = context.getFlagByCharacter(fg);
                            if (flag == null || !flag.isSwitch())
                                throw new CommandParseException(root,
                                        new ErrorLocation(reader, reader.index(), reader.index()),
                                        "No switch flag by character -" + fg);
                            context.flagValues.put(flag, true);

                            // advance
                            reader.next();
                        }
                    }

                    // to next character
                    reader.next();
                }
            }
        }

        // get next node and walk
        Node next = findNext(context, reader);
        if (next == null) /* end of chain */ {
            // suggest
            if (context.isSuggesting()) {
                // suggest flags
                for (Runnable flagSuggestion : flagCompletions) {
                    System.out.println(flagSuggestion);
                    flagSuggestion.run();
                }

                // suggest nodes
                if (last != null) {
                    // use last node to suggest
                    Suggester suggester;
                    if ((suggester = last.getComponentOf(Suggester.class)) != null) {
                        suggester.suggest(context, suggestions, fReader);
                    }
                }
            }

            // return back track
            return executeOrBacktrack(context);
        } else {
            // walk node
            if (next.walk(context, this, reader)) {
                return executeOrBacktrack(context);
            }

            // dont back track further
            return false;
        }
    }

    public Node findNext(Context context, StringReader reader) {
        // try fast mapped executable first
        String name = reader.branch().collect(c -> c != ' ');
        Node node = fastMappedChildren.get(name);
        context.setOption("__fast_node_name", name);
        if (node != null) {
            return node;
        }

        // find highest valid
        Primary highest = null;
        for (Node child : children) {
            Primary primary = child.getComponentOf(Primary.class);
            if (primary.selects(context, reader.branch())) {
                if (highest == null || primary.priority() > highest.priority())
                    highest = primary;
            }
        }

        if (highest != null)
            return highest.getNode();

        // check for end
        if (reader.current() == StringReader.DONE)
            return null;

        // return unknown
        return NODE_UNKNOWN;
    }

    ////////////////////////////////////////////////
    //// CONFIGURATION
    ////////////////////////////////////////////////

    /* Getters. */

    public List<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public Map<String, Node> getFastMappedChildren() {
        return Collections.unmodifiableMap(fastMappedChildren);
    }

    public List<NodeComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public Map<Class<?>, NodeComponent> getComponentsByClass() {
        return Collections.unmodifiableMap(componentsByClass);
    }

    public Node parent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return Collections.unmodifiableList(aliases);
    }

    public Node root() {
        return root;
    }

    /* Aliases. */

    public Node withAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    public Node addAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    public Node removeAlias(String... aliases) {
        this.aliases.removeAll(Arrays.asList(aliases));
        return this;
    }

    /* Components. */

    public <T extends NodeComponent> T makeComponent(Function<Node, T> constructor) {
        return withComponent(constructor.apply(this));
    }

    public <T extends NodeComponent> Node makeComponent(Function<Node, T> constructor,
                                                        Consumer<T> consumer) {
        T it = withComponent(constructor.apply(this));
        if (consumer != null)
            consumer.accept(it);
        return this;
    }

    public <T extends NodeComponent> T component(Class<T> tClass,
                                                 Function<Node, T> constructor) {
        T c = getComponent(tClass);
        if (c != null) {
            return c;
        }

        c = constructor.apply(this);
        withComponent(c);
        return c;
    }

    public <T extends NodeComponent> Node component(Class<T> tClass,
                                                    Function<Node, T> constructor,
                                                    BiConsumer<Node, T> consumer) {
        T c = component(tClass, constructor);
        if (consumer != null)
            consumer.accept(this, c);
        return this;
    }

    public <T extends NodeComponent> T withComponent(T component) {
        Objects.requireNonNull(component, "component cannot be null");
        if (componentsByClass.containsKey(component.getClass()))
            return component;
        components.add(component);
        ReflectionUtil.walkParents(component.getClass(),
                c -> !c.isAssignableFrom(NonComponent.class),
                c -> componentsByClass.put(c, component));
//        ReflectionUtil.getCallerClass(3);
        return component;
    }

    public <T extends NodeComponent> Node withComponent(T component, Consumer<T> consumer) {
        T c = withComponent(component);
        if (consumer != null)
            consumer.accept(c);
        return this;
    }

    public Node removeComponent(NodeComponent component) {
        components.remove(component);
        ReflectionUtil.walkParents(component.getClass(),
                c -> !c.isAssignableFrom(NonComponent.class),
                c -> componentsByClass.remove(c, component));
        return this;
    }

    public Node removeComponent(Class<?> klass) {
        return removeComponent(componentsByClass.get(klass));
    }

    @SuppressWarnings("unchecked")
    public <T> T getComponentOf(Class<T> klass) {
        return (T) componentsByClass.get(klass);
    }

    @SuppressWarnings("unchecked")
    public <T extends NodeComponent> T getComponent(Class<T> klass) {
        return (T) componentsByClass.get(klass);
    }

    public boolean hasComponentOf(Class<?> klass) {
        return componentsByClass.containsKey(klass);
    }

    /* Children. */

    public Node addChild(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        children.add(node);
        childrenByName.put(node.name, node);
        if (node.componentsByClass.containsKey(Executable.class))
            fastMappedChildren.put(node.name, node);
        return node;
    }

    public Node addChild(Node node, Consumer<Node> consumer) {
        consumer.accept(addChild(node));
        return this;
    }

    public Node removeChild(Node node) {
        children.remove(node);
        childrenByName.remove(node.getName());
        if (node.componentsByClass.containsKey(Executable.class))
            fastMappedChildren.remove(node.name);
        return this;
    }

    public Node getChildByName(String name) {
        return childrenByName.get(name);
    }

    public Node getChildByPath(String... path) {
        Node curr = this;
        for (String p : path)
            curr = curr.getChildByName(p);
        return curr;
    }

    public Node getSubnode(String name) {
        return childrenByName.get(name);
    }

    public Node getOrCreateSubnode(String name, Function<Node, Node> constructor) {
        Node node;
        if ((node = getSubnode(name)) != null)
            return node;
        node = constructor.apply(this);
        addChild(node);
        return node;
    }

    public Primary getNextSubnode(Context ctx, StringReader reader) {
        if (reader.current() == StringReader.DONE)
            return null;
        Node node;
        if ((node = fastMappedChildren.get(reader.branch().collect(c -> c != ' '))) != null)
            return node.getComponentOf(Primary.class);
        Primary sel;
        for (Node child : children)
            if ((sel = child.getComponentOf(Primary.class)).selects(ctx, reader.branch()))
                return sel;
        return null;
    }

    public Node processWalked(Context context, StringReader reader) {
        for (NodeComponent component : components)
            if (!(component instanceof Primary) && component instanceof Functional fc)
                fc.walked(context, reader);
        return this;
    }

    public Node processExecute(Context context) {
        for (NodeComponent component : components)
            if (!(component instanceof Primary) && component instanceof Functional fc)
                fc.execute(context);
        return this;
    }

    /* QOL Methods. */

    public Node propertied(String desc, String label, String usage) {
        component(
                Properties.class,
                Properties::new,
                (node, rcp) -> rcp
                        .description(desc)
                        .label(label)
                        .usage(usage)
        );
        return this;
    }

    public Node executable() {
        return executable(null);
    }

    public Node executable(Consumer<Executable> consumer) {
        Executable executable = component(Executable.class, Executable::new);
        if (consumer != null)
            consumer.accept(executable);
        return this;
    }

    public Node executes(CommandNodeExecutor executor) {
        component(Executable.class, Executable::new).setExecutor(executor);
        return this;
    }

    public Node executes(CommandNodeExecutor executor, CommandNodeExecutor walked) {
        component(Executable.class, Executable::new).setExecutor(executor).setWalkExecutor(walked);
        return this;
    }

    public Node argument(ArgumentType<?> type) {
        component(Argument.class, Argument::new).setType(type);
        return this;
    }

    public Node permission(String perm) {
        component(Secure.class, Secure::new, (node, secure) ->
                secure.setPermission(perm));
        return this;
    }

    public Node flag(Flag<?> flag) {
        component(Flags.class, Flags::new, (node, flags) ->
                flags.addFlag(flag));
        return this;
    }

    public Node flag(String name, Character ch, ArgumentType<?> type, boolean isSwitch) {
        component(Flags.class, Flags::new, (node, flags) ->
                flags.addFlag(name, ch, type, isSwitch));
        return this;
    }

    public Node flag(String name, ArgumentType<?> type) {
        component(Flags.class, Flags::new, (node, flags) ->
                flags.addFlag(name, null, type, false));
        return this;
    }

    public Node thenArgument(String name,
                             ArgumentType<?> type) {
        Node node = new Node(name, this, root);
        node.argument(type);
        this.addChild(node);
        return node;
    }

    public Node thenArgument(String name,
                             ArgumentType<?> type,
                             Consumer<Node> consumer) {
        Node node = thenArgument(name, type);
        if (consumer != null)
            consumer.accept(node);
        return this;
    }

    public Node thenArgument(String name,
                             ArgumentType<?> type,
                             BiConsumer<Node, Argument> consumer) {
        Node node = thenArgument(name, type);
        if (consumer != null)
            consumer.accept(node, node.getComponent(Argument.class));
        return this;
    }

    public Node thenExecute(String name, CommandNodeExecutor executor) {
        Node node = new Node(name, this, root);
        node.executes(executor);
        this.addChild(node);
        return node;
    }

    public Node thenExecute(String name, CommandNodeExecutor executor, Consumer<Node> consumer) {
        Node n = thenExecute(name, executor);
        if (consumer != null)
            consumer.accept(n);
        return this;
    }

    public Node thenExecute(String name, CommandNodeExecutor executor, BiConsumer<Node, Executable> consumer) {
        Node n = thenExecute(name, executor);
        if (consumer != null)
            consumer.accept(n, n.getComponent(Executable.class));
        return this;
    }

    public Node thenExecute(String name, CommandNodeExecutor executor, CommandNodeExecutor walked) {
        Node node = new Node(name, this, root);
        node.executes(executor, walked);
        this.addChild(node);
        return node;
    }

    public Node thenExecute(String name, CommandNodeExecutor executor, CommandNodeExecutor walked, BiConsumer<Node, Executable> consumer) {
        Node node = thenExecute(name, executor, walked);
        if (consumer != null)
            consumer.accept(node, node.getComponent(Executable.class));
        return this;
    }

    ////////////////////////////////////////////////

    public void printTreeFancy(PrintStream stream) {
        printTreeFancyNext(stream, 0);
    }

    private void printTreeFancyNext(PrintStream stream,
                                    int depth) {
        if (depth >= 50) {
            stream.println(" ".repeat(depth) + " /!\\ Tree goes too deep! Over 50 entries deep.");
            return;
        }

        Argument param;
        if ((param = getComponent(Argument.class)) != null) { // is parameter
            stream.println(" ".repeat(depth) + "\\" + name + " <" + param.getType().getIdentifier() + " " + param.getIdentifier() + ">");
        } else {
            stream.println(" ".repeat(depth) + "/" + name);
        }

        for (Node child : children) {
            child.printTreeFancyNext(stream, depth + 1);
        }
    }

}
