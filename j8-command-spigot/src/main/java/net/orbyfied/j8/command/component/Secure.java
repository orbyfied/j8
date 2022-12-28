package net.orbyfied.j8.command.component;

import net.orbyfied.j8.command.AbstractNodeComponent;
import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.Node;
import net.orbyfied.j8.util.StringReader;
import org.bukkit.ChatColor;

import java.util.function.Function;

public class Secure extends AbstractNodeComponent implements Functional {

    // permission to check against
    // ignored if null
    Function<Context, String> permissionLock;

    public Secure(Node node) {
        super(node);
    }

    void failSecurity(Context ctx, String s) {
        ctx.fail("You do not have access to this command: " + ChatColor.YELLOW + s);
    }

    @Override
    public void walked(Context ctx, StringReader reader) {
        // perform check: permission node
        String permission;
        if (permissionLock != null && (permission = permissionLock.apply(ctx)) != null)
            if (!ctx.wrappedSender().hasPermission(permission))
                failSecurity(ctx, "Lacking Permission");
    }

    @Override
    public void execute(Context ctx) { }

    /* ---- Getters and Setters ---- */

    public Secure setPermissionLock(Function<Context, String> lock) {
        this.permissionLock = lock;
        return this;
    }

    public Secure setPermission(final String permission) {
        return setPermissionLock(ctx -> permission);
    }

}
