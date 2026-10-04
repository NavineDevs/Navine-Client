package nv.navineclient;

import nv.navineclient.commands.Command;
import nv.navineclient.commands.CommandManager;
import nv.navineclient.config.ConfigManager;
import nv.navineclient.module.Module;
import nv.navineclient.module.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddonManager {
    private static final List<NavineAddon> addons = new ArrayList<>();
    private static final Set<String> disabledAddonPackages = new HashSet<>();
    private static boolean initialized = false;
    
    public static void init() {
        if (initialized) return;
        initialized = true;
        
        addons.clear();
        disabledAddonPackages.clear();

        List<EntrypointContainer<NavineAddon>> containers = FabricLoader.getInstance().getEntrypointContainers("navine", NavineAddon.class);
        
        for (EntrypointContainer<NavineAddon> container : containers) {
            try {
                NavineAddon addon = container.getEntrypoint();
                addons.add(addon);
                NavineClient.LOGGER.info("Loaded addon: " + addon.getClass().getSimpleName() + " from " + container.getProvider().getMetadata().getId());
            } catch (Exception e) {
                NavineClient.LOGGER.error("Failed to load addon from " + container.getProvider().getMetadata().getId(), e);
            }
        }
        
        for (NavineAddon addon : addons) {
            try {
                addon.onRegisterCategories();
            } catch (Exception e) {
                NavineClient.LOGGER.error("Error registering categories for addon: " + addon.getClass().getSimpleName(), e);
            }
        }
        
        for (NavineAddon addon : addons) {
            try {
                addon.onInitialize();
                
                for (Command command : addon.getCommands()) {
                    CommandManager.registerCommand(command);
                }
                
                for (Module module : addon.getModules()) {
                    ModuleManager.registerModule(module);
                }
                
                NavineClient.LOGGER.info("Initialized addon: " + addon.getClass().getSimpleName() + 
                    " (Commands: " + addon.getCommands().size() + ", Modules: " + addon.getModules().size() + ")");
            } catch (Exception e) {
                NavineClient.LOGGER.error("Error initializing addon: " + addon.getClass().getSimpleName(), e);
            }
        }
    }
    
    public static void registerCategories() {
        for (NavineAddon addon : addons) {
            try {
                addon.onRegisterCategories();
            } catch (Exception e) {
                NavineClient.LOGGER.error("Error registering categories for addon: " + addon.getClass().getSimpleName(), e);
            }
        }
    }
    
    public static List<NavineAddon> getAddons() {
        return new ArrayList<>(addons);
    }
    
    public static NavineAddon getAddon(String packageName) {
        return addons.stream()
                .filter(addon -> addon.getPackage().equals(packageName))
                .findFirst()
                .orElse(null);
    }

    public static boolean hasAddons() {
        return !addons.isEmpty();
    }

    public static boolean isAddonEnabled(NavineAddon addon) {
        if (addon == null) return false;
        return !disabledAddonPackages.contains(addon.getPackage());
    }

    public static void loadEnabledState(String packageName, boolean enabled) {
        if (enabled) {
            disabledAddonPackages.remove(packageName);
        } else {
            disabledAddonPackages.add(packageName);
            NavineAddon addon = getAddon(packageName);
            if (addon != null) {
                for (Module module : addon.getModules()) {
                    if (module.isEnabled()) {
                        module.setEnabled(false);
                    }
                }
            }
        }
    }

    public static void setAddonEnabled(NavineAddon addon, boolean enabled) {
        if (addon == null) return;
        if (enabled) {
            disabledAddonPackages.remove(addon.getPackage());
        } else {
            disabledAddonPackages.add(addon.getPackage());
            for (Module module : addon.getModules()) {
                if (module.isEnabled()) {
                    module.setEnabled(false);
                }
            }
        }
        ConfigManager.save();
    }

    public static boolean isModuleFromDisabledAddon(Module module) {
        for (NavineAddon addon : addons) {
            if (addon.getModules().contains(module) && !isAddonEnabled(addon)) {
                return true;
            }
        }
        return false;
    }
}
