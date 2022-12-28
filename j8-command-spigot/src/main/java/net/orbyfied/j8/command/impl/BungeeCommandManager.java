package net.orbyfied.j8.command.impl;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.orbyfied.j8.command.*;
import net.orbyfied.j8.command.component.Properties;
import net.orbyfied.j8.command.minecraft.MinecraftArgumentTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BungeeCommandManager extends CommandManager {

    public static final Sender CONSOLE_SENDER = new Sender() {
        // the command sender
        final CommandSender sender = ProxyServer.getInstance().getConsole();

        @Override
        public void sendMessage(BaseComponent[] components) {
            sender.sendMessage(components);
        }

        @Override
        public boolean hasPermission(String perm) {
            return sender.hasPermission(perm);
        }

        @Override
        public Object unwrap() {
            return sender;
        }
    };

    public static Sender wrapSender(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return new Sender() {
                @Override
                public void sendMessage(BaseComponent[] components) {
                    player.sendMessage(components);
                }

                @Override
                public boolean hasPermission(String perm) {
                    return player.hasPermission(perm);
                }

                @Override
                public Object unwrap() {
                    return player;
                }
            };
        } else {
            return CONSOLE_SENDER;
        }
    }

    private final Plugin plugin;

    // the fallback prefix
    private String fallbackPrefix;

    public BungeeCommandManager(Plugin plugin) {
        super();
        this.plugin = plugin;
        this.fallbackPrefix = plugin.getDescription().getName().toLowerCase(Locale.ROOT);

        ((DelegatingNamespacedTypeResolver)getTypeResolver())
                .namespace("minecraft", MinecraftArgumentTypes.typeResolver);
    }

    public BungeeCommandManager setFallbackPrefix(String fallbackPrefix) {
        this.fallbackPrefix = fallbackPrefix;
        return this;
    }

    public String getFallbackPrefix() {
        return fallbackPrefix;
    }

    // registered commands by node name
    Map<String, RegisteredBungeeCommand> bungeeCommandMap = new HashMap<>();

    @Override
    protected void registerPlatform(Node root) {
        RegisteredBungeeCommand command = new RegisteredBungeeCommand(this, root);
        bungeeCommandMap.put(root.getName(), command);
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, command);
    }

    @Override
    protected void unregisterPlatform(Node root) {
        RegisteredBungeeCommand command = bungeeCommandMap.get(root.getName());
        if (command == null) return;
        ProxyServer.getInstance().getPluginManager().unregisterCommand(command);
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

    static class RegisteredBungeeCommand extends Command implements TabExecutor {
        protected final CommandManager engine;
        protected final Node node;

        protected RegisteredBungeeCommand(CommandManager engine,
                                          Node node) {
            super(node.getName(),
                    "",
                    node.getAliases().toArray(new String[0]));

            // set fields
            this.engine = engine;
            this.node   = node;
        }

        @Override
        public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
            String str = stitchArgs(getName(), args);
            Context ctx = engine.dispatch(wrapSender(sender), str, null, null);
            if (ctx.intermediateText() != null && ctx.intermediateText().length != 0)
                sender.sendMessage(ctx.intermediateText());
        }

        @Override
        public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
            List<String> list = new ArrayList<>();
            String str = stitchArgs(getName(), args);
            Context ctx = engine.dispatch(wrapSender(sender), str, createSuggestionAccumulator(list), null);
            return list;
        }

    }

}
