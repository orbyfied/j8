package net.orbyfied.j8.command;

import net.orbyfied.j8.command.component.Executable;
import net.orbyfied.j8.command.component.Selecting;
import net.orbyfied.j8.command.component.Suggester;
import net.orbyfied.j8.command.exception.*;
import net.orbyfied.j8.command.impl.DelegatingNamespacedTypeResolver;
import net.orbyfied.j8.command.impl.SystemParameterType;
import net.orbyfied.j8.command.parameter.Flag;
import net.orbyfied.j8.command.parameter.Parameter;
import net.orbyfied.j8.command.parameter.TypeResolver;
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
public abstract class CommandEngine {

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

    public CommandEngine() {
        typeResolver = new DelegatingNamespacedTypeResolver()
                .namespace("system",    SystemParameterType.typeResolver);
    }

    public CommandEngine register(Node command) {
        commands.add(command);
        aliases.put(command.getName(), command);
        for (String alias : command.aliases)
            aliases.put(alias, command);
        registerPlatform(command);
        return this;
    }

    public CommandEngine unregister(Node command) {
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
    public CommandEngine setTypeResolver(TypeResolver resolver) {
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
        Context.Destiny destiny = (isSuggesting ? Context.Destiny.SUGGEST : Context.Destiny.EXECUTE);

        // create string reader
        StringReader reader = new StringReader(str, 0);

        // create context
        Context context = new Context(this, sender);
        context.destiny(destiny);
        if (ctxConsumer != null)
            ctxConsumer.accept(context);
        context.successful(true);

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

            // walk root
            Executable lastExecutable = null; // the executable to execute at the end
            Selecting mainc = root.getComponentOf(Selecting.class); // bring out to walk root too
            Suggester suggester = null; // last suggester
            Node current = root;
            while (true) {

                // update context
                context.current = current;
                context.currentExecutable = lastExecutable;

                // call walked
                if (mainc != null)
                    mainc.getNode().processWalked(context, reader);

                // is executable
                if (mainc instanceof Executable exec) {
                    lastExecutable = exec;
                    try {
                        // execute walked
                        exec.walked(context, reader);
                    } catch (Exception e) {
                        // dont create a massive chain of exceptions
                        if (e instanceof CommandException) {
                            // throw the exception itself
                            throw (RuntimeException)e;
                        } else {
                            // throw the execution exception
                            throw new NodeExecutionException(root, current, e);
                        }
                    }

                    // is parameter
                } else if (mainc instanceof Parameter param) {
                    // parse and save parameter
                    param.walked(context, reader);
                }

                // parse flags
                while (reader.peek(1) == '-') {
                    int sidx = reader.index();

                    // named flag
                    if (reader.next(2) == '-') {
                        // parse flag name
                        // and get flag
                        reader.next();
                        String flagName = reader.collect(c -> c != '=' && c != ' ');
                        Flag<?> flag    = context.flagsByName.get(flagName);
                        if (flag == null) {
                            if (!isSuggesting) { // throw error if flag was not found
                                throw new CommandParseException(root,
                                        new ErrorLocation(reader, sidx + 1, reader.index()),
                                        "Flag --" + flagName + " was not found.");
                            } else {
                                for (Flag<?> f : context.flags)
                                    if (f.isSwitch())
                                        suggestions.suggest("--" + f.getName());
                                    else
                                        suggestions.suggest("--" + f.getName() + "=");
                                reader.collect(c -> c != ' ');
                            }
                        } else {
                            // parse value or switch
                            if (reader.current() == '=') {
                                // parse value
                                reader.next();
                                if (!isSuggesting) {
                                    Object val = flag.getType().parse(context, reader);

                                    // set flag
                                    context.flagValues.put(flag, val);
                                } else {
                                    // suggest values from type
                                    suggestions.pushPrefix("--" + flagName + "=");
                                    flag.getType().suggest(context, suggestions);
                                    suggestions.popPrefix();
                                }
                            } else {
                                // check switch
                                if (!flag.isSwitch())
                                    throw new CommandParseException(root,
                                            new ErrorLocation(reader, sidx + 1, reader.index()),
                                            "Flag --" + flagName + " is not a switch, but no value was provided.");

                                // parse switch
                                context.flagValues.put(flag, true);
                            }
                        }
                    } else {
                        // get the characters in the specifier
                        if (!isSuggesting) {
                            char c;
                            while ((c = reader.current()) != ' ' && c != StringReader.DONE) {
                                Flag<?> flag = context.flagsByChar.get(c);
                                if (flag == null)
                                    throw new CommandParseException(root,
                                            new ErrorLocation(reader, sidx + 1, reader.index()),
                                            "Flag -" + c + " (switch) was not found.");

                                // enable switch
                                if (!flag.isSwitch())
                                    throw new CommandParseException(root,
                                            new ErrorLocation(reader, sidx + 1, reader.index()),
                                            "Flag -" + c + " is not a switch, but no value was provided.");

                                context.flagValues.put(flag, true);

                                // advance
                                reader.next();
                            }
                        } else {
                            for (Map.Entry<Character, Flag<?>> entry : context.flagsByChar.entrySet())
                                suggestions.suggest("-" + entry.getKey());
                        }
                    }

                }

                // suggest
                Suggester tempSuggester;
                if (isSuggesting && (tempSuggester = current.getComponentOf(Suggester.class)) != null)
                    suggester = tempSuggester;

                // skip to next character
                reader.next();

                // error handling
                int idx = reader.index();
                char cb = reader.current();

                // get main functional component
                // and set current to new node
                mainc = current.getNextSubnode(context, reader);

                // unknown subcommand
                if (!isSuggesting && mainc == null && cb != StringReader.DONE) {
                    throw new NodeParseException(root, current, new ErrorLocation(reader, idx - 1, reader.index()),
                            "Unknown subcommand.");
                }

                // break if we ended
                if (reader.current() == StringReader.DONE || mainc == null) {
                    current = null;
                    break;
                }

                // get current node
                current = mainc.getNode();

            }

            // suggest
            if (isSuggesting && suggester != null)
                suggester.suggestNext(
                        context,
                        suggestions,
                        reader,
                        current
                );

            // execute
            if (lastExecutable != null && !isSuggesting && context.successful()) {
                try {
                    // call execute on the node
                    lastExecutable.getNode().processExecute(context);

                    // execute final command
                    lastExecutable.execute(context);
                } catch (Exception e) {
                    // dont create a massive chain of exceptions
                    if (e instanceof CommandException) {
                        // throw the exception itself
                        throw (RuntimeException)e;
                    } else {
                        // throw the execution exception
                        throw new NodeExecutionException(root, lastExecutable.node, e);
                    }
                }
            }

        } catch (CommandException e) {
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
            } else {
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
        node.root = node;
        register(node);
        return node;
    }

}
