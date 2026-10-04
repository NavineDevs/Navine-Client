package nv.navineclient.module;

import net.minecraft.client.Minecraft;
import nv.navineclient.module.settings.Setting;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final Minecraft mc = Minecraft.getInstance();
    private final String name;
    private final String description;
    private final Category category;
    private final String customCategoryId;
    private boolean enabled;
    private int key = 0;
    public boolean keyHeld = false;
    private final List<Setting> settings = new ArrayList<>();

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.customCategoryId = null;
    }

    public Module(String name, String description, String customCategoryId) {
        this.name = name;
        this.description = description;
        this.category = null;
        this.customCategoryId = customCategoryId;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean usesCustomCategory() { return customCategoryId != null; }
    public String getCustomCategoryId() { return customCategoryId; }
    public boolean isVisibleInClickGui() { return true; }
    public boolean isEnabled() { return enabled; }
    public int getKey() { return key; }
    public void setKey(int key) { this.key = key; }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            onEnable();
            nv.navineclient.util.ToggleNotifier.show(name + " Enabled");
        } else {
            onDisable();
            nv.navineclient.util.ToggleNotifier.show(name + " Disabled");
        }
    }

    public void toggle() { setEnabled(!enabled); }

    protected void setDefaultEnabled() { this.enabled = true; }
    public void onTick() {}
    public void onEnable() {}
    public void onDisable() {}

    protected void addSetting(Setting setting) {
        settings.add(setting);
    }

    public List<Setting> getSettings() { return settings; }

    public boolean isSettingVisible(Setting<?> setting) {
        return true;
    }

    public enum Category {
        COMBAT, MOVEMENT, PLAYER, RENDER, WORLD, MISC;

        public String getDisplayName() {
            String name = name();
            return name.charAt(0) + name.substring(1).toLowerCase();
        }
        
        public static Category[] orderedValues() {
            return new Category[]{COMBAT, MOVEMENT, PLAYER, RENDER, WORLD, MISC};
        }
    }
}
