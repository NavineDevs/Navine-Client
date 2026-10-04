package nv.navineclient.commands;

import nv.navineclient.util.AuthManager;
import nv.navineclient.util.ChatUtils;
import nv.navineclient.util.IRCClient;
import java.util.ArrayList;
import java.util.List;

public class OwnerCommand extends Command {
    public OwnerCommand() {
        super("owner", "Owner specific commands", ".owner announce <message>");
    }

    @Override
    public List<String> getCompletions(String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            if ("announce".startsWith(partial)) {
                completions.add("announce");
            }
        }
        return completions;
    }

    @Override
    public boolean isVisibleToUser() {
        return AuthManager.canUseOwnerCommands();
    }

    @Override
    public void onCommand(String[] args) {
        if (!AuthManager.canUseOwnerCommands()) {
            if (!AuthManager.isVerified()) {
                ChatUtils.error("Not logged in. Use .login HitBoyXx23 (owner is client account, not Minecraft IGN).");
            } else {
                ChatUtils.error("You are not an owner. Account: " + AuthManager.getLoggedInUser()
                        + " | " + AuthManager.getStatusSummary());
                ChatUtils.error("Logout and login with an owner account: .logout then .login HitBoyXx23");
            }
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("announce")) {
            if (args.length < 2) {
                ChatUtils.info("Usage: " + getSyntax());
                return;
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) {
                    builder.append(' ');
                }
                builder.append(args[i]);
            }
            String msg = builder.toString().trim();
            if (msg.isEmpty()) {
                ChatUtils.info("Usage: " + getSyntax());
                return;
            }
            ChatUtils.message("§5[Navine Announcement] §f" + msg);
            IRCClient.sendOwnerAnnounce(msg);
            return;
        }

        ChatUtils.info("Usage: " + getSyntax());
        ChatUtils.message("§7" + AuthManager.getStatusSummary());
    }
}
