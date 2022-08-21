package net.orbyfied.j8tp;

import net.orbyfied.j8.command.CommandManager;
import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.Node;
import net.orbyfied.j8.command.annotation.BaseCommand;
import net.orbyfied.j8.command.annotation.CommandParameter;
import net.orbyfied.j8.command.annotation.Subcommand;
import net.orbyfied.j8.command.impl.BukkitCommandManager;
import net.orbyfied.j8.command.argument.ArgumentTypes;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class J8TestPlugin extends JavaPlugin {

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        // register commands
//        new BaseAnnotationProcessor(commandManager, new HelloCommand()).compile().register();

        Node helloCmd = new Node("hello", null, null)
                .thenExecute("hi", (ctx, cmd) -> {
                    ctx.sender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                            ctx.getFlagValue("hi", String.class)));
                })
                .flag("hi", ArgumentTypes.STRING)
                .root()
                .thenExecute("yo", (context, cmd) -> {
                    String name = context.getArgument("yo:name");
                    Integer num = context.getArgument("yo:num");

                    if (context.getFlagValue("fb", Boolean.class, false)) {
                        context.sender().sendMessage("hi " + name + "-" + num);
                    } else {
                        context.sender().sendMessage("hello " + name + "-" + num);
                    }

                    String sus;
                    if ((sus = context.getFlagValue("fa")) != null)
                        context.sender().sendMessage("SUS: " + sus);

                    context.sender().sendMessage("fc: " + context.getFlagValue("fc"));
                    context.sender().sendMessage("fd: " + context.getFlagValue("fd"));
                })
                .permission("yo.mama")
                .flag("fa", ArgumentTypes.STRING)
                .flag("fb", 'b', ArgumentTypes.BOOLEAN, true)
                .flag("fc", ArgumentTypes.DOUBLE)
                .flag("fd", ArgumentTypes.LIST.instance(ArgumentTypes.VECTOR_3F))
                .thenArgument("name", ArgumentTypes.STRING)
                .thenArgument("num", ArgumentTypes.INT)
                .root();

        commandManager.register(helloCmd);
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
