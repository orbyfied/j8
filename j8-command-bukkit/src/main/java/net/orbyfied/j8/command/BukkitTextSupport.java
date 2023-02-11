package net.orbyfied.j8.command;

import org.bukkit.ChatColor;

import java.awt.*;

public class BukkitTextSupport {

    // translates the format specifier
    // from the string to a chat color
    // used in bukkit legacy text
    public static Object translateFormatSpec(String fmtSpec) {
        if (fmtSpec.startsWith("#")) {
            Color color = new Color(
                    Integer.parseInt(fmtSpec.substring(0, 2), 16),
                    Integer.parseInt(fmtSpec.substring(2, 4), 16),
                    Integer.parseInt(fmtSpec.substring(4, 6), 16)
            );

            return net.md_5.bungee.api.ChatColor.of(color);
        }

        return ChatColor.getByChar(fmtSpec);
    }

}
