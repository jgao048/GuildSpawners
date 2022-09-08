package dev.failures.guildspawners.Utils;

import org.bukkit.ChatColor;

public class ChatUtil {
    public static String colorize(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
