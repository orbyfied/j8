import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.Node;
import net.orbyfied.j8.command.annotation.BaseCommand;
import net.orbyfied.j8.command.annotation.CommandParameter;
import net.orbyfied.j8.command.annotation.Subcommand;

@BaseCommand(name = "sus")
public class Sus {

    @Subcommand("exec <system:bool hi> --hello/h(system:bool s)")
    public void executeSus(Context ctx, Node node,

                           @CommandParameter("hi") String hi) {

    }

}
