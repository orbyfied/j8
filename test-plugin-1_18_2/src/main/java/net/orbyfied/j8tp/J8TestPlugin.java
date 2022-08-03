package net.orbyfied.j8tp;

import net.orbyfied.j8.command.CommandEngine;
import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.component.Flags;
import net.orbyfied.j8.command.Node;
import net.orbyfied.j8.command.annotation.BaseCommand;
import net.orbyfied.j8.command.annotation.CommandParameter;
import net.orbyfied.j8.command.annotation.Subcommand;
import net.orbyfied.j8.command.impl.BukkitCommandEngine;
import net.orbyfied.j8.command.impl.SystemParameterType;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class J8TestPlugin extends JavaPlugin {

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        // register commands
//        new BaseAnnotationProcessor(commandEngine, new HelloCommand()).compile().register();

        Node helloCmd = new Node("hello", null, null)
                .thenExecute("hi", (ctx, cmd) -> {
                    ctx.sender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                            ctx.getFlagValue("hi", String.class)));
                })
                .flag("hi", SystemParameterType.STRING)
                .root()
                .thenExecute("yo", (context, cmd) -> {
                    String name = context.getSymbol("yo:name");
                    Integer num = context.getSymbol("yo:num");

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
                .flag("fa", SystemParameterType.STRING)
                .flag("fb", 'b', SystemParameterType.BOOLEAN, true)
                .flag("fc", SystemParameterType.DOUBLE)
                .flag("fd", SystemParameterType.LIST.instance(SystemParameterType.VEC_3_INT))
                .thenParameter("name", SystemParameterType.STRING)
                .thenParameter("num", SystemParameterType.INT)
                .root();

        commandEngine.register(helloCmd);
    }

    @Override
    public void onDisable() {

    }

    /////////////////////////////////

    {
        this.commandEngine = new BukkitCommandEngine(this);
    }

    final CommandEngine commandEngine;

    public CommandEngine getCommandEngine() {
        return commandEngine;
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
