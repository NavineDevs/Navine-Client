package nv.navineclient.commands;

import nv.navineclient.util.ClientAccess;

import com.mojang.blaze3d.platform.InputConstants;

import nv.navineclient.module.Module;
import nv.navineclient.module.ModuleManager;
import nv.navineclient.util.ChatUtils;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "Displays all commands with their syntax", ".help [command|modules]", "h", "?");
    }

    @Override
    public void onCommand(String[] args) {
        String prefix = CommandManager.getPrefix();

        if (args.length == 0) {
            if (!CommandManager.isShowDotCommands()) {
                ChatUtils.message("§eDot-command list is hidden. Enable §fShow . Commands§e in Navine Settings.");
                ChatUtils.message("§7Commands still work when typed. Autocomplete is off.");
                return;
            }
            List<Command> listed = CommandManager.getListedCommands();
            ChatUtils.message("§b§l=== [Navine Client] Commands (" + listed.size() + ") ===");
            ChatUtils.message("§7Prefix §f" + prefix + "§7  · type bare §f" + prefix + "§7 for autocomplete");
            ChatUtils.message("§7Use §f" + prefix + "help <command>§7 for details");
            ChatUtils.message("");

            for (int i = 0; i < listed.size(); i += 2) {
                Command a = listed.get(i);
                String left = "§f" + prefix + a.getName() + " §8- §7" + truncate(a.getDescription(), 22);
                if (i + 1 < listed.size()) {
                    Command b = listed.get(i + 1);
                    String right = "§f" + prefix + b.getName() + " §8- §7" + truncate(b.getDescription(), 22);
                    ChatUtils.message(left + "  " + right);
                } else {
                    ChatUtils.message(left);
                }
            }

            ChatUtils.message("");
            ChatUtils.message("§7Auth: §f" + prefix + "login §7| §f" + prefix + "logout");
            ChatUtils.message("§7IRC: §f" + prefix + "irc connect §7| §f" + prefix + "irc logs §7| §f" + prefix + "irc say <msg>");
            ChatUtils.message("§7Type §f" + prefix + "help modules§7 to see all modules");
            return;
        }

        String arg = args[0].toLowerCase();

        if (arg.equals("modules")) {
            ChatUtils.message("§b§l=== All Modules (" + ModuleManager.getModules().size() + ") ===");
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Module m : ModuleManager.getModules()) {
                String status = m.isEnabled() ? "§a" : "§7";
                sb.append(status).append(m.getName()).append("§f, ");
                count++;
                if (count % 5 == 0) {
                    ChatUtils.message(sb.toString());
                    sb = new StringBuilder();
                }
            }
            if (sb.length() > 0) {
                ChatUtils.message(sb.substring(0, sb.length() - 2));
            }
            ChatUtils.message("§7Green = enabled, Gray = disabled");
            return;
        }

        for (Command cmd : CommandManager.getCommands()) {
            if (cmd.getName().equalsIgnoreCase(arg)) {
                if (!cmd.isVisibleToUser()) {
                    ChatUtils.message("§cUnknown command or module: " + arg);
                    return;
                }
                ChatUtils.message("§b§l=== " + cmd.getName() + " ===");
                ChatUtils.message("§7Description: §f" + cmd.getDescription());
                ChatUtils.message("§7Usage: §f" + cmd.getSyntax());
                if (cmd.getAliases().length > 0) {
                    ChatUtils.message("§7Aliases: §f" + String.join(", ", cmd.getAliases()));
                }
                return;
            }
        }

        Module module = ModuleManager.getModuleByName(arg);
        if (module != null) {
            ChatUtils.message("§b§l=== " + module.getName() + " ===");
            ChatUtils.message("§7Description: §f" + module.getDescription());
            ChatUtils.message("§7Status: " + (module.isEnabled() ? "§aEnabled" : "§cDisabled"));
            if (module.getKey() != 0) {
                ChatUtils.message("§7Keybind: §f" + ClientAccess.keyName(module.getKey()));
            }
            if (!module.getSettings().isEmpty()) {
                ChatUtils.message("§7Settings:");
                for (var setting : module.getSettings()) {
                    ChatUtils.message("  §f" + setting.getName() + "§7: " + setting.getValue());
                }
            }
            return;
        }

        ChatUtils.message("§cUnknown command or module: " + arg);
        ChatUtils.message("§7Use §f" + prefix + "help§7 for a list of commands");
    }

    @Override
    public List<String> getCompletions(String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            if ("modules".startsWith(input)) {
                completions.add("modules");
            }
            for (Command cmd : CommandManager.getListedCommands()) {
                if (cmd.getName().toLowerCase().startsWith(input)) {
                    completions.add(cmd.getName());
                }
            }
            for (Module m : ModuleManager.getModules()) {
                if (m.getName().toLowerCase().startsWith(input)) {
                    completions.add(m.getName());
                }
            }
        }
        return completions;
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, Math.max(0, max - 1)) + "…";
    }
}
