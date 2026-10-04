package nv.navineclient.commands;

import nv.navineclient.util.AuthManager;
import nv.navineclient.util.ChatUtils;

public class LoginCommand extends Command {
    public LoginCommand() {
        super("login", "Login to Navine client account", ".login <username>");
    }

    @Override
    public void onCommand(String[] args) {
        if (args.length < 1) {
            ChatUtils.message("§b§l=== Navine Login ===");
            ChatUtils.message("§fUsage: §7.login <username>");
            ChatUtils.message("§fUsage: §7.login autologin <on|off>");
            ChatUtils.message("");
            ChatUtils.message("§7Client account is separate from your Minecraft IGN.");
            ChatUtils.message("§7" + AuthManager.getStatusSummary());
            ChatUtils.message("§7Auto-login: " + (nv.navineclient.config.ConfigManager.isAutoLoginEnabled() ? "§aon" : "§coff"));
            ChatUtils.message("§7Use §f.logout§7 to sign out and clear the saved session.");
            return;
        }

        if (args[0].equalsIgnoreCase("autologin")) {
            if (args.length < 2) {
                ChatUtils.message("§7Auto-login is currently " + (nv.navineclient.config.ConfigManager.isAutoLoginEnabled() ? "§aon" : "§coff"));
                ChatUtils.message("§7Usage: §f.login autologin <on|off>");
                return;
            }
            boolean enable = args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true");
            boolean disable = args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false");
            if (!enable && !disable) {
                ChatUtils.message("§cUse §fon§c or §foff§c.");
                return;
            }
            nv.navineclient.config.ConfigManager.setAutoLoginEnabled(enable);
            ChatUtils.message("§aAuto-login " + (enable ? "enabled" : "disabled") + ".");
            if (!enable) {
                ChatUtils.message("§7You will need to log in from the title screen on each launch.");
            }
            return;
        }

        String username = args[0];

        if (AuthManager.verify(username)) {
            ChatUtils.message("§a§l=== Login Successful ===");
            ChatUtils.message("§7" + AuthManager.getStatusSummary());
            ChatUtils.message("");

            if (AuthManager.isOwner()) {
                ChatUtils.message("§d[OWNER] §7You have owner privileges (account-based).");
            } else if (AuthManager.isDev()) {
                ChatUtils.message("§b[DEV] §7You have developer privileges!");
            } else {
                ChatUtils.message("§f[Navine] §7You are a verified Navine user!");
            }

            ChatUtils.message("");
            ChatUtils.message("§7Use §f.irc connect§7 to join the IRC chat");
            ChatUtils.message("§7Press §fRight Shift§7 to open ClickGUI");
        } else {
            ChatUtils.message("§c§l=== Login Failed ===");
            ChatUtils.message("§cUsername '§f" + username + "§c' is not whitelisted.");
            ChatUtils.message("§7Contact an admin to get access.");
        }
    }
}
