package net.orbyfied.j8tp;

import net.orbyfied.j8.command.CommandManager;
import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.Node;
import net.orbyfied.j8.command.annotation.BaseCommand;
import net.orbyfied.j8.command.annotation.CommandParameter;
import net.orbyfied.j8.command.annotation.Subcommand;
import net.orbyfied.j8.command.impl.BukkitCommandManager;
import net.orbyfied.j8.command.argument.ArgumentTypes;
import net.orbyfied.j8.command.minecraft.MinecraftArgumentTypes;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class J8TestPlugin extends JavaPlugin {

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        // register commands
//        new BaseAnnotationProcessor(commandManager, new HelloCommand()).compile().register();

        Node helloCmd = commandManager.command("hello")
                .executes((ctx, cmd) -> {
                    ctx.bukkitSender().sendMessage("hehehhehehehheheehehhehhe");
                })
                .thenExecute("hi", (ctx, cmd) -> {
                    ctx.bukkitSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                            ctx.getFlagValue("hi", String.class)));
                })
                .flag("hi", ArgumentTypes.STRING)
                .root()
                .thenExecute("a", (ctx, cmd) -> {
                    ctx.bukkitSender().sendMessage("a");
                })
                .thenArgument("player", MinecraftArgumentTypes.ONLINE_PLAYER_DIRECT)
                .thenExecute("b", (ctx, cmd) -> {
                    ctx.bukkitSender().sendMessage(ctx.<Player>getArgument("player").getName());
                })
                .root()
                .thenExecute("yo", (context, cmd) -> {
                    String name = context.getArgument("name");
                    Integer num = context.getArgument("num");

                    if (context.getFlagValue("fb", Boolean.class, false)) {
                        context.bukkitSender().sendMessage("hi " + name + "-" + num);
                    } else {
                        context.bukkitSender().sendMessage("hello " + name + "-" + num);
                    }

                    String sus;
                    if ((sus = context.getFlagValue("fa")) != null)
                        context.bukkitSender().sendMessage("SUS: " + sus);

                    context.bukkitSender().sendMessage("fc: " + context.getFlagValue("fc"));
                    context.bukkitSender().sendMessage("fd: " + context.getFlagValue("fd"));
                })
                .permission("yo.mama")
                .flag("fa", ArgumentTypes.STRING)
                .flag("fb", 'b', ArgumentTypes.BOOLEAN, true)
                .flag("fc", ArgumentTypes.DOUBLE)
                .flag("fd", MinecraftArgumentTypes.ONLINE_PLAYER_DIRECT)
                .thenArgument("name", ArgumentTypes.STRING)
                .thenArgument("num", ArgumentTypes.INT)
                .thenExecute("amogus", (ctx, cmd) -> {
                    ctx.halt(true, "amogus-name: " + ctx.getArgument("name"));
                })
                .root();
    }

    @Override
    public void onDisable() {

    }

    /////////////////////////////////

    {
        this.commandManager = new BukkitCommandManager(this);
    }

    final CommandManager commandManager;

    public CommandManager getCommandEngine() {
        return commandManager;
    }

    ////////////////////////////////

    @BaseCommand(name = "hello")
    static class HelloCommand {
        //
        @Subcommand("<system:string name> (system:bool sayhi/h /) (system:string sus) <system:int num>")
        void hello(Context context, Node node,

                   @CommandParameter("name") String name,
                   @CommandParameter("num")  Integer num){

        }

    }

}
