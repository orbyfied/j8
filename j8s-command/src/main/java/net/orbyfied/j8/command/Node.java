package net.orbyfied.j8.command;

import net.orbyfied.j8.command.component.*;
import net.orbyfied.j8.command.component.Properties;
import net.orbyfied.j8.command.impl.CommandNodeExecutor;
import net.orbyfied.j8.command.argument.Flag;
import net.orbyfied.j8.command.argument.Argument;
import net.orbyfied.j8.command.argument.ArgumentType;
import net.orbyfied.j8.util.ReflectionUtil;
import net.orbyfied.j8.util.StringReader;

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
    }

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
        return addComponent(constructor.apply(this));
    }

    public <T extends NodeComponent> Node makeComponent(Function<Node, T> constructor,
                                                        Consumer<T> consumer) {
        T it = addComponent(constructor.apply(this));
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
        addComponent(c);
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

    public <T extends NodeComponent> T addComponent(T component) {
        Objects.requireNonNull(component, "component cannot be null");
        components.add(component);
        ReflectionUtil.walkParents(component.getClass(),
                c -> !c.isAssignableFrom(NonComponent.class),
                c -> componentsByClass.put(c, component));
        return component;
    }

    public <T extends NodeComponent> Node addComponent(T component, Consumer<T> consumer) {
        T c = addComponent(component);
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

    public Node executes(CommandNodeExecutor executor) {
        addComponent(new Executable(this)).setExecutor(executor);
        return this;
    }

    public Node executes(CommandNodeExecutor executor, CommandNodeExecutor walked) {
        addComponent(new Executable(this)).setExecutor(executor).setWalkExecutor(walked);
        return this;
    }

    public Node argument(ArgumentType<?> type) {
        addComponent(new Argument(this)).setType(type);
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

    public Node thenExecute(String name, CommandNodeExecutor executor, CommandNodeExecutor walked) {
        Node node = new Node(name, this, root);
        node.executes(executor, walked);
        this.addChild(node);
        return node;
    }

    ////////////////////////////////////////////////

    public void printTreeFancy(PrintStream stream) {
        printTreeFancyNext(stream, 0);
    }

    private void printTreeFancyNext(PrintStream stream,
                                    int depth) {
        if (depth >= 50) {
            System.out.println(" ".repeat(depth) + " /!\\ Tree goes too deep! Over 50 entries deep.");
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
