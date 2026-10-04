package nv.navineclient.commands;

import nv.navineclient.module.Module;
import nv.navineclient.module.ModuleManager;
import nv.navineclient.util.ChatUtils;

public class PanicCommand extends Command {
    public PanicCommand() {
        super("panic", "Disables all active modules", ".panic");
    }

    @Override
    public void onCommand(String[] args) {
        int count = 0;
        for (Module m : ModuleManager.getModules()) {
            if (m.isEnabled()) {
                m.setEnabled(false);
                count++;
            }
        }
        ChatUtils.message("§cPanic! Disabled §f" + count + " §cmodules.");
    }
}
