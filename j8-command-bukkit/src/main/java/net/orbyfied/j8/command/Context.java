package net.orbyfied.j8.command;

import net.md_5.bungee.api.ChatColor;
import net.orbyfied.j8.command.argument.Argument;
import net.orbyfied.j8.command.argument.options.ArgumentOptions;
import net.orbyfied.j8.command.component.Executable;
import net.orbyfied.j8.command.exception.CommandHaltException;
import net.orbyfied.j8.command.argument.Flag;
import net.orbyfied.j8.registry.Identifier;
import net.orbyfied.j8.util.StringReader;
import org.bukkit.command.CommandSender;

import java.util.*;

public class Context {

    public Context(CommandManager engine,
                   CommandSender sender) {
        this.engine = engine;
        this.sender = sender;
    }

    /**
     * The sender of the command.
     */
    protected final CommandSender sender;

    /**
     * The purspose of this invocation.
     */
    protected Target target;

    /**
     * The root command node.
     */
    protected Node rootCommand;

    /**
     * The list of argument values.
     */
    protected final HashMap<Identifier, Object> argValues = new HashMap<>();

    /**
     * The command engine.
     */
    protected final CommandManager engine;

    /**
     * The intermediate status text.
     */
    protected String intermediateText;

    /**
     * If the text can be formatted.
     */
    protected boolean canFormat = true;

    /**
     * If the invocation was successful.
     */
    protected Boolean successful;

    /**
     * The current string reader used for parsing.
     */
    protected StringReader reader;

    /**
     * The current node we are at.
     */
    protected Node current;

    /**
     * All flags registered.
     */
    protected List<Flag<?>> flags = new ArrayList<>();

    /**
     * All registered flags by name.
     */
    protected Map<String, Flag<?>> flagsByName = new HashMap<>();

    /**
     * All one-char-able flags by character.
     */
    protected Map<Character, Flag<?>> flagsByChar = new HashMap<>();

    /**
     * The flag values.
     */
    protected Map<Flag<?>, Object> flagValues = new HashMap<>();

    /**
     * The foreign (unregistered) flag values.
     * Always either a string or boolean.
     */
    protected Map<String, Object> foreignFlagValues = new HashMap<>();

    /**
     * The suggestion accumulator.
     */
    protected SuggestionAccumulator suggestions;

    /* ----- Basic Manipulation ----- */

    public Context canFormat(boolean canFormat) {
        this.canFormat = canFormat;
        return this;
    }

    public CommandManager manager() {
        return engine;
    }

    @SuppressWarnings("unchecked")
    public <S extends CommandSender> S sender() {
        return (S) sender;
    }

    public boolean senderIs(Class<? extends CommandSender> cClass) {
        return cClass.isAssignableFrom(sender.getClass());
    }

    public Target target() {
        return target;
    }

    public boolean isSuggesting() {
        return target == Target.SUGGEST;
    }

    public String intermediateText() {
        return intermediateText;
    }

    public Context intermediateText(String text) {
        if (!canFormat)
            text = ChatColor.stripColor(text);
        this.intermediateText = text;
        return this;
    }

    public Context successful(boolean b) {
        this.successful = b;
        return this;
    }

    public Boolean successful() {
        return successful;
    }

    public Context target(Target target) {
        this.target = target;
        return this;
    }

    public SuggestionAccumulator suggestions() {
        return suggestions;
    }

    public Node rootCommand() {
        return rootCommand;
    }

    public StringReader reader() {
        return reader;
    }

    public Node currentNode() {
        return current;
    }

    @SuppressWarnings("unchecked")
    public <O extends ArgumentOptions> O argumentOptions(Class<O> oClass) {
        Argument argument = currentNode().getComponent(Argument.class);
        ArgumentOptions options;
        if (argument == null || (options = argument.getOptions()) == null) return null;
        if (!oClass.isAssignableFrom(options.getClass()))
            return null;
        return (O) options;
    }

    public Context halt(boolean success, String message) {
        throw new CommandHaltException(rootCommand, message)
                .setSuccessful(success);
    }

    public Context halt(boolean success, Throwable t) {
        throw new CommandHaltException(rootCommand, t)
                .setSuccessful(success);
    }

    public Context halt(boolean success, String message, Throwable t) {
        throw new CommandHaltException(rootCommand, message, t)
                .setSuccessful(success);
    }

    public Context fail(String message) {
        return halt(false, message);
    }

    public Context fail(Throwable t) {
        return halt(false, t);
    }

    public Context fail(String message, Throwable t) {
        return halt(false, message, t);
    }

    /* ----- Symbols ----- */

    public HashMap<Identifier, Object> getArgumentValues() {
        return argValues;
    }

    @SuppressWarnings("unchecked")
    public <T> T getArgument(Identifier identifier) {
        return (T) argValues.get(identifier);
    }

    public <T> T getArgument(String id) {
        return getArgument(Identifier.of(id));
    }

    @SuppressWarnings("unchecked")
    public <T> T getArgument(Identifier identifier, Class<T> tClass) {
        return (T) argValues.get(identifier);
    }

    public <T> T getArgument(String id, Class<T> tClass) {
        return getArgument(Identifier.of(id), tClass);
    }

    public boolean hasArgument(Identifier identifier) {
        return argValues.containsKey(identifier);
    }

    public boolean hasArgument(String id) {
        return hasArgument(Identifier.of(id));
    }

    public Context setArgument(Identifier id, Object o) {
        argValues.put(id, o);
        return this;
    }

    public Context setArgument(String id, Object o) {
        return setArgument(Identifier.of(id), o);
    }

    public Context unsetArgument(Identifier id) {
        argValues.remove(id);
        return this;
    }

    public Context unsetArgument(String id) {
        return unsetArgument(Identifier.of(id));
    }

    /* ----- Flags ----- */

    public Context pushFlag(Flag<?> flag) {
        flags.add(flag);
        flagsByName.put(flag.getName(), flag);
        Character c = flag.getCharacter();
        if (c != null)
            flagsByChar.put(c, flag);
        return this;
    }

    public List<Flag<?>> getFlags() {
        return flags;
    }

    public Flag<?> getFlagByName(String name) {
        return flagsByName.get(name);
    }

    public Flag<?> getFlagByCharacter(char c) {
        return flagsByChar.get(c);
    }

    @SuppressWarnings("unchecked")
    public <T> T getFlagValue(Flag<T> flag) {
        if (flag == null)
            return null;
        T res;
        if (!flagValues.containsKey(flag))
            res = flag.getDefault();
        else
            res = (T) flagValues.get(flag);
        return res;
    }

    @SuppressWarnings("unchecked")
    public <T> T getFlagValue(String name) {
        Flag<?> flag = flagsByName.get(name);
        if (flag == null)
            return (T) foreignFlagValues.get(name);
        else
            return (T) getFlagValue(flag);
    }

    @SuppressWarnings("unchecked")
    public <T> T getFlagValue(String name, Class<T> tClass) {
        return (T) getFlagValue(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getFlagValue(Flag<?> flag, T ifUnset) {
        if (flag == null)
            return ifUnset;
        if (!flagValues.containsKey(flag))
            return ifUnset;
        return (T) getFlagValue(flag);
    }

    @SuppressWarnings("unchecked")
    public <T> T getFlagValue(Flag<?> flag, Class<T> tClass, T ifUnset) {
        if (flag == null)
            return ifUnset;
        if (!flagValues.containsKey(flag))
            return ifUnset;
        return (T) getFlagValue(flag);
    }

    @SuppressWarnings("unchecked")
    public <T> T getFlagValue(String name, Class<T> tClass, T ifUnset) {
        Flag<?> flag = flagsByName.get(name);
        if (flag == null)
            return (T) foreignFlagValues.getOrDefault(name, ifUnset);
        else
            return getFlagValue(flag, ifUnset);
    }

    ///////////////////////////////////

    /**
     * Declares the purposes/destinies
     * of an invocation.
     * @see Context#target
     */
    public enum Target {

        SUGGEST,
        EXECUTE

    }

}
