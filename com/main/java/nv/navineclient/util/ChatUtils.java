package nv.navineclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ChatUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    public static void message(String message) {
        Component component = Component.literal("§8[§9Navine Client§8] §f" + message);
        if (mc.player != null) {
            mc.player.sendSystemMessage(component);
        } else if (mc.gui != null) {
            ClientAccess.getChat(mc).addClientSystemMessage(component);
        }
    }

    public static void info(String message) {
        message(message);
    }

    public static void error(String message) {
        message("§c" + message);
    }

    public static void success(String message) {
        message("§a" + message);
    }
    
    public static void sendChat(String message) {
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.sendChat(message);
        }
    }
    
    public static void sendCommand(String command) {
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.sendCommand(command);
        }
    }

    public static Component format(String message) {
        return Component.literal("§8[§9Navine Client§8] §f" + message);
    }

    public static String getNavineTag(String username) {
        // Use AuthManager for consistency
        return nv.navineclient.util.AuthManager.getTagForPlayer(username).trim() + "§r";
    }

    public static String formatChatMessage(String username, String message) {
        return getNavineTag(username) + " " + username + ": " + message;
    }

    public static String formatDM(String sender, String message) {
        return "§7[DM] " + getNavineTag(sender) + " " + sender + " §7→ You: " + message;
    }

    public static String formatTabName(String username) {
        return getNavineTag(username) + " " + username;
    }
}
