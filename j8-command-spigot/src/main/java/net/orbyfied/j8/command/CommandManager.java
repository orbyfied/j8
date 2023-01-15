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
import net.md_5.bungee.api.ChatColor;
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
                .namespace("system", ArgumentTypes.typeResolver);
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
    public Context dispatch(Sender sender,
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
        Node node = new Node(name, null, null);
        node.executable();
        node.root = node;
        if (consumer != null)
            consumer.accept(node);
        register(node);
        return node;
    }

}
