package net.orbyfied.j8.command.impl;

import net.orbyfied.j8.command.*;
import net.orbyfied.j8.command.component.Properties;
import net.orbyfied.j8.command.minecraft.MinecraftArgumentTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Uses the Bukkit command system to register
 * and execute/tab-complete commands.
 * TODO: find a way to get the message above the textbox
 */
public class BukkitCommandManager extends CommandManager {

    private static final SimpleCommandMap commandMap = (SimpleCommandMap) Bukkit.getCommandMap();

    private final Plugin plugin;

    // the fallback prefix
    private String fallbackPrefix;

    public BukkitCommandManager(Plugin plugin) {
        super();
        this.plugin = plugin;
        this.fallbackPrefix = plugin.getName().toLowerCase(Locale.ROOT);

        ((DelegatingNamespacedTypeResolver)getTypeResolver())
                .namespace("minecraft", MinecraftArgumentTypes.typeResolver);
    }

    public BukkitCommandManager setFallbackPrefix(String fallbackPrefix) {
        this.fallbackPrefix = fallbackPrefix;
        return this;
    }

    public String getFallbackPrefix() {
        return fallbackPrefix;
    }

    @Override
    protected void registerPlatform(Node root) {
        RegisteredBukkitCommand cmd = new RegisteredBukkitCommand(this, root);
        commandMap.register(cmd.getLabel(), fallbackPrefix, cmd);
    }

    @Override
    protected void unregisterPlatform(Node root) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enablePlatform() {

    }

    @Override
    public void disablePlatform() {
        
    }

    private static String stitchArgs(String label, String[] args) {
        String[] ls = label.split(":");
        String   l  = ls.length == 1 ? ls[0] : ls[1];
        StringBuilder b = new StringBuilder(l);
        for (String s : args)
            b.append(" ").append(s);
        return b.toString();
    }

    private static SuggestionAccumulator createSuggestionAccumulator(List<String> list) {
        return new SuggestionAccumulator() {
            @Override
            public void suggest0(String s) {
                list.add(s);
            }

            @Override
            public void unsuggest0(String o) {
                if (o != null)
                    list.remove(o);
            }
        };
    }

    static class RegisteredBukkitCommand extends BukkitCommand {

        protected final CommandManager engine;
        protected final Node node;

        protected RegisteredBukkitCommand(CommandManager engine,
                                          Node node) {
            super(node.getName(),
                    "",
                    "",
                    node.getAliases());

            // set fields
            this.engine = engine;
            this.node   = node;

            // set properties
            Properties rcp = node.getComponentOf(Properties.class);
            if (rcp != null) {
                if (rcp.description() != null)
                    this.setDescription(rcp.description());
                if (rcp.label() != null)
                    this.setLabel(rcp.label());
                if (rcp.usage() != null)
                    this.setUsage(rcp.usage());
            }
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
            String str = stitchArgs(alias, args);
            Context ctx = engine.dispatch(sender, str, null, null);
            if (ctx.intermediateText() != null && !ctx.intermediateText().isBlank())
                sender.sendMessage(ctx.intermediateText());
            return ctx.successful();
        }

        @Override
        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, Location location) throws IllegalArgumentException {
            List<String> list = new ArrayList<>();
            String str = stitchArgs(alias, args);
            Context ctx = engine.dispatch(sender, str, createSuggestionAccumulator(list), null);
            return list;
        }

    }

}