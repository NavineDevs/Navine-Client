package nv.navineclient.config;

import nv.navineclient.util.ClientAccess;

import com.mojang.blaze3d.platform.InputConstants;

import nv.navineclient.AddonManager;
import nv.navineclient.NavineAddon;
import nv.navineclient.NavineClient;
import nv.navineclient.util.AuthManager;
import nv.navineclient.module.Module;
import nv.navineclient.module.ModuleManager;
import nv.navineclient.module.settings.Setting;
import nv.navineclient.module.settings.BooleanSetting;
import nv.navineclient.module.settings.NumberSetting;
import nv.navineclient.module.settings.ModeSetting;
import nv.navineclient.module.settings.ColorSetting;
import net.minecraft.client.Minecraft;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final File CONFIG_DIR = new File(Minecraft.getInstance().gameDirectory, "Navine");
    private static final File MODULE_CONFIG = new File(CONFIG_DIR, "modules.txt");
    private static final Map<String, int[]> clickGuiTabPositions = new HashMap<>();
    private static boolean autoLogin = true;
    private static int modulesVisible = 11;
    private static boolean hudTopLeft = true;
    private static boolean notificationCenterHotbar = true;
    private static boolean showDotCommands = true;

    public static void save() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter(MODULE_CONFIG))) {
            writer.println("GUIKEY:" + NavineClient.getClickGuiKey());
            writer.println("AUTOLOGIN:" + autoLogin);
            writer.println("MODULESVISIBLE:" + modulesVisible);
            writer.println("HUDTOPLEFT:" + hudTopLeft);
            writer.println("NOTIFCENTER:" + notificationCenterHotbar);
            writer.println("SHOWDOTCOMMANDS:" + showDotCommands);
            if (AuthManager.isVerified() && AuthManager.getLoggedInUser() != null) {
                writer.println("SESSION:" + AuthManager.getLoggedInUser());
            }

            for (NavineAddon addon : AddonManager.getAddons()) {
                writer.println("ADDON:" + addon.getPackage() + ":" + AddonManager.isAddonEnabled(addon));
            }

            for (Map.Entry<String, int[]> entry : clickGuiTabPositions.entrySet()) {
                int[] pos = entry.getValue();
                if (pos != null && pos.length >= 2) {
                    writer.println("TABPOS:" + entry.getKey() + ":" + pos[0] + ":" + pos[1]);
                }
            }

            for (Module m : ModuleManager.getModules()) {
                writer.println(m.getName() + ":" + m.isEnabled() + ":" + m.getKey());
                for (Setting<?> s : m.getSettings()) {
                    if (s instanceof ModeSetting ms) {
                        writer.println("S:" + m.getName() + ":" + s.getName() + ":" + ms.getValue());
                    } else {
                        writer.println("S:" + m.getName() + ":" + s.getName() + ":" + s.getValue());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!MODULE_CONFIG.exists()) return;
        String pendingSession = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(MODULE_CONFIG))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts[0].equals("S") && parts.length >= 4) {
                    Module module = ModuleManager.getModuleByName(parts[1]);
                    if (module != null) {
                        for (Setting<?> s : module.getSettings()) {
                            if (s.getName().equalsIgnoreCase(parts[2])) {
                                if (s instanceof BooleanSetting bs) {
                                    bs.setValue(Boolean.parseBoolean(parts[3]));
                                } else if (s instanceof NumberSetting ns) {
                                    ns.setValue(Double.parseDouble(parts[3]));
                                } else if (s instanceof ModeSetting ms) {
                                    ms.setValue(parts[3]);
                                } else if (s instanceof ColorSetting cs) {
                                    try {
                                        cs.setValue(Integer.parseInt(parts[3]));
                                    } catch (NumberFormatException e) {
                                    }
                                }
                            }
                        }
                    }
                } else if (parts[0].equals("TABPOS") && parts.length >= 4) {
                    try {
                        clickGuiTabPositions.put(parts[1], new int[]{
                                Integer.parseInt(parts[2]),
                                Integer.parseInt(parts[3])
                        });
                    } catch (NumberFormatException ignored) {
                    }
                } else if (parts[0].equals("SESSION") && parts.length >= 2) {
                    pendingSession = parts[1];
                } else if (parts[0].equals("AUTOLOGIN") && parts.length >= 2) {
                    autoLogin = Boolean.parseBoolean(parts[1]);
                } else if (parts[0].equals("ADDON") && parts.length >= 3) {
                    AddonManager.loadEnabledState(parts[1], Boolean.parseBoolean(parts[2]));
                } else if (parts[0].equals("GUIKEY") && parts.length >= 2) {
                    try {
                        int loadedKey = Integer.parseInt(parts[1]);
                        if (loadedKey == InputConstants.KEY_I) {
                            loadedKey = InputConstants.KEY_RSHIFT;
                        }
                        NavineClient.setClickGuiKey(loadedKey, false);
                    } catch (NumberFormatException e) {
                    }
                } else if (parts[0].equals("MODULESVISIBLE") && parts.length >= 2) {
                    try {
                        modulesVisible = clampModulesVisible(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException ignored) {
                    }
                } else if (parts[0].equals("HUDTOPLEFT") && parts.length >= 2) {
                    hudTopLeft = Boolean.parseBoolean(parts[1]);
                } else if (parts[0].equals("NOTIFCENTER") && parts.length >= 2) {
                    notificationCenterHotbar = Boolean.parseBoolean(parts[1]);
                } else if (parts[0].equals("SHOWDOTCOMMANDS") && parts.length >= 2) {
                    showDotCommands = Boolean.parseBoolean(parts[1]);
                } else if (parts.length >= 3) {
                    Module m = ModuleManager.getModuleByName(parts[0]);
                    if (m != null) {
                        m.setEnabled(Boolean.parseBoolean(parts[1]));
                        m.setKey(Integer.parseInt(parts[2]));
                    }
                }
            }
            if (autoLogin && pendingSession != null) {
                AuthManager.restoreSession(pendingSession);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isAutoLoginEnabled() {
        return autoLogin;
    }

    public static void setAutoLoginEnabled(boolean enabled) {
        autoLogin = enabled;
        save();
    }

    public static void saveSession() {
        save();
    }

    public static int[] getClickGuiTabPosition(String tabId) {
        return clickGuiTabPositions.get(tabId);
    }

    public static void setClickGuiTabPosition(String tabId, int x, int y) {
        clickGuiTabPositions.put(tabId, new int[]{x, y});
    }

    public static int getModulesVisible() {
        return modulesVisible;
    }

    public static void setModulesVisible(int count) {
        modulesVisible = clampModulesVisible(count);
        save();
    }

    public static int getModuleListMaxHeight() {
        return getModulesVisible() * 15 + 2;
    }

    public static boolean isHudTopLeft() {
        return hudTopLeft;
    }

    public static void setHudTopLeft(boolean topLeft) {
        hudTopLeft = topLeft;
        save();
    }

    public static boolean isNotificationCenterHotbar() {
        return notificationCenterHotbar;
    }

    public static void setNotificationCenterHotbar(boolean centerHotbar) {
        notificationCenterHotbar = centerHotbar;
        save();
    }

    public static boolean isShowDotCommands() {
        return showDotCommands;
    }

    public static void setShowDotCommands(boolean enabled) {
        showDotCommands = enabled;
        save();
    }

    private static int clampModulesVisible(int count) {
        return Math.max(8, Math.min(15, count));
    }
}
