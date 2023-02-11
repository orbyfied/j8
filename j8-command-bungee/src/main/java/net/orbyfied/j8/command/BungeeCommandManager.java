package net.orbyfied.j8.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.orbyfied.j8.command.impl.DelegatingNamespacedTypeResolver;

import java.util.*;

public class BungeeCommandManager extends CommandManager {

    public static final Sender CONSOLE_SENDER = new Sender() {
        // the command sender
        final CommandSender sender = ProxyServer.getInstance().getConsole();

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
                .namespace("bungee", BungeeArgumentTypes.typeResolver);
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
        String prefix = fallbackPrefix != null ? fallbackPrefix + ":" : "";
        String fullName = prefix + root.getName();
        List<String> aliases = new ArrayList<>();
        aliases.add(root.getName());
        root.getAliases().forEach(s -> {
            aliases.add(s);
            aliases.add(prefix + s);
        });
        RegisteredBungeeCommand command = new RegisteredBungeeCommand(fullName, aliases, this, root);
        bungeeCommandMap.put(command.fullName, command);
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
        protected String fullName;
        protected final BungeeCommandManager engine;
        protected final Node node;

        protected RegisteredBungeeCommand(String fullName,
                                          List<String> aliases,
                                          BungeeCommandManager engine,
                                          Node node) {
            super(fullName,
                    "",
                    aliases.toArray(new String[0]));

            // set fields
            this.fullName = fullName;
            this.engine = engine;
            this.node   = node;
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            String str = stitchArgs(getName(), args);
            Context ctx = engine.dispatch(wrapSender(sender), str, null, null);
            if (ctx.intermediateText() != null && ctx.intermediateText().length() != 0)
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
