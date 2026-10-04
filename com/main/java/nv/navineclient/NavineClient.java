package nv.navineclient;

import nv.navineclient.util.ClientAccess;

import com.mojang.blaze3d.platform.InputConstants;

import nv.navineclient.commands.CommandManager;
import nv.navineclient.config.ConfigManager;
import nv.navineclient.module.Module;
import nv.navineclient.module.ModuleManager;
import nv.navineclient.ui.ClickGUI;
import nv.navineclient.util.AuthManager;
import nv.navineclient.util.BackgroundManager;
import nv.navineclient.util.MenuMusicManager;
import nv.navineclient.util.DiscordNativeLoader;
import nv.navineclient.util.ToggleNotifier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavineClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("navine-client");
    public static final String MOD_ID = "navine-client";
    public static final String VERSION = "1.6.5";
    
    private static boolean clickGuiKeyHeld = false;
    private static boolean bigRadarKeyHeld = false;
    private static int clickGuiKey = InputConstants.KEY_RSHIFT;
    private static boolean windowTitleApplied = false;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Navine Client v{} Initializing...", VERSION);
        BackgroundManager.loadExternalBackground();
        MenuMusicManager.ensureLoaded();
        DiscordNativeLoader.load();
        AuthManager.fetchUsers();
        AddonManager.init();
        ModuleManager.init();
        CommandManager.init();
        ConfigManager.load();
        enforceDevModAccess();
        
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            nv.navineclient.util.PlaceConflictGuard.reset();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!windowTitleApplied && client.getWindow() != null) {
                client.getWindow().setTitle(buildWindowTitle());
                windowTitleApplied = true;
            }
            if (client.getWindow() == null) return;

            MenuMusicManager.tick();

            boolean guiPressed = ClientAccess.isKeyDown(clickGuiKey);
            if (!(ClientAccess.getScreen(client) instanceof ClickGUI)) {
                if (guiPressed && !clickGuiKeyHeld) {
                    clickGuiKeyHeld = true;
                    if (ClientAccess.getScreen(client) == null) {
                        ClickGUI gui = new ClickGUI();
                        gui.prepareForOpen();
                        ClientAccess.setScreen(client, gui);
                    }
                } else if (!guiPressed) {
                    clickGuiKeyHeld = false;
                }
            } else if (!guiPressed) {
                clickGuiKeyHeld = false;
            }

            nv.navineclient.module.render.Radar radar = (nv.navineclient.module.render.Radar) ModuleManager.getModuleByName("Radar");
            if (AuthManager.isVerified() && radar != null && radar.isEnabled() && radar.isBigRadarEnabled()) {
                boolean mPressed = ClientAccess.isKeyDown(InputConstants.KEY_M);
                if (mPressed && !bigRadarKeyHeld && ClientAccess.getScreen(client) == null) {
                    bigRadarKeyHeld = true;
                    ClientAccess.setScreen(client, new nv.navineclient.ui.BigRadarScreen());
                } else if (!mPressed) {
                    bigRadarKeyHeld = false;
                }
            }

            for (Module module : ModuleManager.getModules()) {
                if (module.isEnabled()) {
                    if (ModuleManager.isModuleAccessible(module)) {
                        module.onTick();
                    } else {
                        module.setEnabled(false);
                    }
                }
            }

            nv.navineclient.module.render.Zoom zoom = nv.navineclient.module.render.Zoom.INSTANCE;
            if (zoom != null && !zoom.isEnabled() && zoom.needsTick()) {
                zoom.onTick();
            }

            for (Module module : ModuleManager.getModules()) {
                if (module.getKey() != 0 && ClientAccess.getScreen(client) == null) {
                    if (!ModuleManager.canToggleModule(module)) continue;
                    boolean pressed = ClientAccess.isKeyDown(module.getKey());
                    if (module instanceof nv.navineclient.module.render.Zoom zoomModule && zoomModule.useHoldKey()) {
                        zoomModule.setEnabled(pressed);
                        zoomModule.setKeyHeld(pressed);
                        module.keyHeld = pressed;
                        if (zoomModule.needsTick()) {
                            zoomModule.onTick();
                        }
                        continue;
                    }
                    if (pressed && !module.keyHeld) {
                        module.keyHeld = true;
                        module.toggle();
                        if (!module.getName().equalsIgnoreCase("Flashstep")) {
                            String state = module.isEnabled() ? "Enabled" : "Disabled";
                            ToggleNotifier.show(module.getName() + " " + state);
                        }
                        ConfigManager.save();
                    } else if (!pressed) {
                        module.keyHeld = false;
                    }
                }
            }
            
            nv.navineclient.commands.SpectateCommand.tick();
            nv.navineclient.util.NavinePathfinder.tick();
            nv.navineclient.module.movement.Blink.tickRelease();
        });
    }
    
    public static int getClickGuiKey() {
        return clickGuiKey;
    }
    
    public static void setClickGuiKey(int key) {
        setClickGuiKey(key, true);
    }
    
    public static void setClickGuiKey(int key, boolean save) {
        clickGuiKey = key;
        if (save) {
            ConfigManager.save();
        }
    }

    public static String buildWindowTitle() {
        return "Navine Client | v" + VERSION + " | " + SharedConstants.getCurrentVersion().name();
    }

    private static void enforceDevModAccess() {
        Module devMod = ModuleManager.getModuleByName("DevMod");
        if (devMod != null && devMod.isEnabled() && !devMod.isVisibleInClickGui()) {
            devMod.setEnabled(false);
        }
    }
}
