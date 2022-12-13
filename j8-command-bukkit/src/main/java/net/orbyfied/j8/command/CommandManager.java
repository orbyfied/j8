package net.orbyfied.j8.command;

import net.orbyfied.j8.command.component.Completer;
import net.orbyfied.j8.command.component.Executable;
import net.orbyfied.j8.command.component.Primary;
import net.orbyfied.j8.command.component.Suggester;
import net.orbyfied.j8.command.exception.*;
import net.orbyfied.j8.command.impl.DelegatingNamespacedTypeResolver;
import net.orbyfied.j8.command.argument.ArgumentTypes;
import net.orbyfied.j8.command.argument.Flag;
import net.orbyfied.j8.command.argument.Argument;
import net.orbyfied.j8.command.argument.TypeResolver;
import net.orbyfied.j8.util.StringReader;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The engine of the command system.
 */
public abstract class CommandManager {

    /**
     * The parameter type resolver.
     */
    TypeResolver typeResolver;

    /**
     * The commands registered.
     */
    ArrayList<Node> commands = new ArrayList<>();

    /**
     * The aliases registered mapped
     * to the nodes.
     */
    HashMap<String, Node> aliases = new HashMap<>();

    public CommandManager() {
        typeResolver = new DelegatingNamespacedTypeResolver()
                .namespace("system",    ArgumentTypes.typeResolver);
    }

    public CommandManager register(Node command) {
        commands.add(command);
        aliases.put(command.getName(), command);
        for (String alias : command.aliases)
            aliases.put(alias, command);
        registerPlatform(command);
        return this;
    }

    public CommandManager unregister(Node command) {
        commands.remove(command);
        aliases.remove(command.getName(), command);
        for (String alias : command.aliases)
            aliases.remove(alias, command);
        unregisterPlatform(command);
        return this;
    }

    /**
     * Set the type resolver.
     * @param resolver It.
     * @return This.
     */
    public CommandManager setTypeResolver(TypeResolver resolver) {
        this.typeResolver = resolver;
        return this;
    }

    /**
     * Get the type resolver.
     * @return It.
     */
    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

    /**
     * Should do whatever it needs to do when
     * a node gets registered to make it work.
     * @param root The command node.
     */
    protected abstract void registerPlatform(Node root);

    /**
     * Should do whatever it needs to do when
     * a node gets unregistered to make it work.
     * @param root The command node.
     */
    protected abstract void unregisterPlatform(Node root);

    /**
     * Should prepare the command engine for
     * usage. Called whenever the system
     * is ready for operation.
     */
    public abstract void enablePlatform();

    /**
     * Should shut down the command engine
     * and clean up any resources.
     */
    public abstract void disablePlatform();

    /**
     * Dispatches a suggestion or invocation
     * request for a command.
     * @param sender The command sender.
     * @param str The command string.
     * @param suggestions The suggestion builder.
     *                    If this is null, the request is invocation,
     *                    otherwise the request will be set to suggestion.
     * @param ctxConsumer A consumer for configuring the context.
     * @return The context for optional further use.
     */
    public Context dispatch(CommandSender sender,
                            String str,
                            SuggestionAccumulator suggestions,
                            Consumer<Context> ctxConsumer) {

        // get mode (execute or suggest)
        boolean isSuggesting    = suggestions != null;
        Context.Target target = (isSuggesting ? Context.Target.SUGGEST : Context.Target.EXECUTE);

        // create string reader
        StringReader reader = new StringReader(str, 0);

        // create context
        Context context = new Context(this, sender);
        context.target(target);
        if (ctxConsumer != null)
            ctxConsumer.accept(context);
        context.successful(true);
        context.suggestions = suggestions;
        if (suggestions != null)
            suggestions.withContext(context);

        // parse alias and get command
        String alias = reader.collect(c -> c != ' ', 0);
        final Node root = aliases.get(alias);
        if (root == null) { // handle if no command exists
            // TODO: better error handling
            return null;
        }

        context.rootCommand = root;
        context.reader      = reader;

        // error handling
        try {

            root.walk(context, null, reader.index(0).branch());

//            // walk root
//            Executable lastExecutable = null; // the executable to execute at the end
//            Primary primary = root.getComponentOf(Primary.class); // bring out to walk root too
//            Suggester suggester = null; // last suggester
//            // nodes
//            Node current = root;
//            Node last    = root;
//
//            // loop
//            while (true) {
//
//                // next iteration
//                last = current;
//
//                // update context
//                context.current = current;
//                context.currentExecutable = lastExecutable;
//
//                // pre
//                char pc = reader.current();
//
//                // call walked
//                if (primary != null)
//                    primary.getNode().processWalked(context, reader);
//
//                // is executable
//                if (primary instanceof Executable exec) {
//                    lastExecutable = exec;
//                    try {
//                        // execute walked
//                        exec.walked(context, reader);
//                    } catch (Exception e) {
//                        // dont create a massive chain of exceptions
//                        if (e instanceof CommandException) {
//                            // throw the exception itself
//                            throw (RuntimeException)e;
//                        } else {
//                            // throw the execution exception
//                            throw new NodeExecutionException(root, current, e);
//                        }
//                    }
//                } else if (primary instanceof Argument param) {
//                    // parse and save parameter
//                    param.walked(context, reader);
//                }
//
//                ///////////////////////////
//                /////// FLAGS
//                //////////////////////////
//
//                while (reader.peek(1) == '-') {
//                    int sidx = reader.index();
//
//                    // named flag
//                    if (reader.next(2) == '-') {
//                        // parse flag name
//                        // and get flag
//                        reader.next();
//                        String flagName = reader.collect(c -> c != '=' && c != ' ');
//                        Flag<?> flag    = context.flagsByName.get(flagName);
//                        if (flag == null) {
//                            if (!isSuggesting) { // throw error if flag was not found
//                                throw new CommandParseException(root,
//                                        new ErrorLocation(reader, sidx + 1, reader.index()),
//                                        "Flag --" + flagName + " was not found.");
//                            } else {
//                                for (Flag<?> f : context.flags)
//                                    if (f.isSwitch())
//                                        suggestions.suggest("--" + f.getName());
//                                    else
//                                        suggestions.suggest("--" + f.getName() + "=");
//                                reader.collect(c -> c != ' ');
//                            }
//                        } else {
//                            // parse value or switch
//                            if (reader.current() == '=') {
//                                // parse value
//                                reader.next();
//                                if (!isSuggesting) {
//                                    Object val = flag.getType().parse(context, reader);
//
//                                    // set flag
//                                    context.flagValues.put(flag, val);
//                                } else {
//                                    // suggest values from type
//                                    suggestions.pushPrefix("--" + flagName + "=");
//                                    flag.getType().suggest(context, suggestions);
//                                    suggestions.popPrefix();
//                                }
//                            } else {
//                                // check switch
//                                if (!flag.isSwitch())
//                                    throw new CommandParseException(root,
//                                            new ErrorLocation(reader, sidx + 1, reader.index()),
//                                            "Flag --" + flagName + " is not a switch, but no value was provided.");
//
//                                // parse switch
//                                context.flagValues.put(flag, true);
//                            }
//                        }
//                    } else {
//                        // get the characters in the specifier
//                        if (!isSuggesting) {
//                            char c;
//                            while ((c = reader.current()) != ' ' && c != StringReader.DONE) {
//                                Flag<?> flag = context.flagsByChar.get(c);
//                                if (flag == null)
//                                    throw new CommandParseException(root,
//                                            new ErrorLocation(reader, sidx + 1, reader.index()),
//                                            "Flag -" + c + " (switch) was not found.");
//
//                                // enable switch
//                                if (!flag.isSwitch())
//                                    throw new CommandParseException(root,
//                                            new ErrorLocation(reader, sidx + 1, reader.index()),
//                                            "Flag -" + c + " is not a switch, but no value was provided.");
//
//                                context.flagValues.put(flag, true);
//                            }
//                        } else {
//                            for (Map.Entry<Character, Flag<?>> entry : context.flagsByChar.entrySet())
//                                suggestions.suggest("-" + entry.getKey());
//                        }
//                    }
//
//                }
//
//                ///////////////////////////
//                /////// NEXT
//                //////////////////////////
//
//                // suggestions
//                Suggester tempSuggester;
//                if (isSuggesting && (tempSuggester = current.getComponentOf(Suggester.class)) != null)
//                    suggester = tempSuggester;
//
//                // skip space
//                reader.collect(c -> c == ' ');
//
//                // error handling
//                int idx = reader.index();
//                char cb = reader.current();
//
//                // check if we are done
//                if (reader.current() == StringReader.DONE) {
//                    if (pc == StringReader.DONE)
//                        current = null;
//                    break;
//                }
//
//                // get new primary component
//                // and set current to new node
//                primary = current.getNextSubnode(context, reader);
//
//                // unknown subcommand
//                if (!isSuggesting && primary == null) {
//                    throw new NodeParseException(
//                            root, current,
//                            new ErrorLocation(reader, idx - 1, reader.index()),
//                            "Unknown subcommand."
//                    );
//                }
//
//                // break if we ended
//                if (primary == null) {
//                    current = null;
//                    break;
//                }
//
//                // get current node
//                current = primary.getNode();
//
//            }
//
//            /////////////////////////////////////
//            /////// SUGGESTIONS
//            /////////////////////////////////////
//
//            if (isSuggesting) {
////                System.out.println(
////                        "current: " + (current != null ? current.getName() : "<null>")
////                        + ", last: " + (last != null ? last.getName() : "<null>")
////                        + ", char: '" + reader.current() + "' (@ " + reader.index() + ")"
////                );
//
//                // no node to complete, suggest
//                // following nodes with last node
//                if (current == null || reader.prev() == ' ') {
//                    // get suggester
//                    Suggester sug = last.getComponent(Suggester.class);
//                    // use default suggester if no component is defined
//                    if (sug == null) {
//                        // complete all children
//                        for (Node child : last.getChildren()) {
//                            // get completer component
//                            Completer comp;
//                            if ((comp = child.getComponent(Completer.class)) != null)
//                                // invoke completions
//                                comp.complete(context, suggestions, reader.branch());
//                        }
//                    } else {
//                        // call defined suggester
//                        sug.suggest(context, suggestions, reader.branch());
//                    }
//                } else {
//                    // get completer component
//                    Completer comp;
//                    if ((comp = current.getComponent(Completer.class)) != null)
//                        // invoke completions
//                        comp.complete(context, suggestions, reader.branch());
//                }
//            }
//
//            // execute
//            if (lastExecutable != null && !isSuggesting && context.successful()) {
//                try {
//                    // call execute on the node
//                    lastExecutable.getNode().processExecute(context);
//
//                    // execute final command
//                    lastExecutable.execute(context);
//                } catch (Exception e) {
//                    // dont create a massive chain of exceptions
//                    if (e instanceof CommandException) {
//                        // throw the exception itself
//                        throw (RuntimeException)e;
//                    } else {
//                        // throw the execution exception
//                        throw new NodeExecutionException(root, lastExecutable.node, e);
//                    }
//                }
//            }

        } catch (CommandException e) {
            // handle halt
            if (e instanceof CommandHaltException ec) {
                boolean success = ec.isSuccessful();

                // create intermediate text message
                // if we have a message or cause to
                // show to the player
                if (ec.getMessage() != null || ec.getCause() != null) {
                    // create intermediate text
                    StringBuilder b = new StringBuilder();
                    if (success)
                        b.append(ChatColor.GREEN + "" + ChatColor.BOLD + "✔ " + ChatColor.GREEN);
                    else
                        b.append(ChatColor.RED + "" + ChatColor.BOLD + "✖ " + ChatColor.RED);
                    if (ec.getMessage() != null)
                        b.append(ec.getMessage());
                    if (ec.getCause() != null)
                        b.append(ChatColor.DARK_GRAY).append(" (").append(ChatColor.RED)
                                .append(ec.getCause()).append(ChatColor.DARK_GRAY).append(")");

                    context.intermediateText(b.toString());
                }

                // set success
                context.successful(success);
            } else /* other error */ {
                // print stack trace if severe enough
                if (e.isSevere())
                    e.printStackTrace();

                // communicate with sender
                context.intermediateText(e.getFormattedString());
                context.successful(false); // fail
            }
        }

        // return
        return context;

    }

    /**
     * Creates and registers a new command,
     * returning the base to you.
     * @param name The name.
     * @return The command node.
     */
    public Node command(String name) {
        Node node = new Node(name, null, null);
        node.executable();
        node.root = node;
        register(node);
        return node;
    }

    public Node newCommand(String name, Consumer<Node> consumer) {
        Node n = command(name);
        if (consumer != null)
            consumer.accept(n);
        return n;
    }

}
