package nv.navineclient.module.combat;

import nv.navineclient.module.Module;
import nv.navineclient.module.ModuleManager;
import nv.navineclient.util.AcBypassUtil;
import nv.navineclient.module.settings.BooleanSetting;
import nv.navineclient.module.settings.NumberSetting;
import net.minecraft.client.Minecraft;

public class Velocity extends Module {
    public static Velocity INSTANCE;
    private final NumberSetting horizontal = new NumberSetting("Horizontal", "Horizontal knockback %", 0.0, 0.0, 100.0);
    private final NumberSetting vertical = new NumberSetting("Vertical", "Vertical knockback %", 0.0, 0.0, 100.0);
    private final NumberSetting chance = new NumberSetting("Chance", "Activation chance %", 100.0, 0.0, 100.0);
    private final BooleanSetting horizontalOnly = new BooleanSetting("HorizontalOnly", "Only reduce horizontal knockback", false);

    private boolean chancePassed = true;
    private long lastChanceRollTick = -1L;

    public Velocity() {
        super("Velocity", "Reduces knockback taken", Category.COMBAT);
        INSTANCE = this;
        addSetting(horizontal);
        addSetting(vertical);
        addSetting(chance);
        addSetting(horizontalOnly);
    }

    public void rollChance() {
        Minecraft client = Minecraft.getInstance();
        long tick = client.level != null ? client.level.getGameTime() : System.currentTimeMillis() / 50L;
        if (tick == lastChanceRollTick) {
            return;
        }
        lastChanceRollTick = tick;
        chancePassed = Math.random() * 100.0 <= chance.getValue();
    }

    @Override
    public void onEnable() {
        chancePassed = true;
        lastChanceRollTick = -1L;
    }

    @Override
    public void onDisable() {
        chancePassed = true;
        lastChanceRollTick = -1L;
    }

    public double getHorizontalMultiplier() {
        if (!chancePassed) {
            return 1.0;
        }
        return AcBypassUtil.minVelocityPercent(horizontal.getValue()) / 100.0;
    }

    public double getVerticalMultiplier() {
        if (!chancePassed) {
            return 1.0;
        }
        if (horizontalOnly.getValue()) {
            return 1.0;
        }
        return AcBypassUtil.minVelocityPercent(vertical.getValue()) / 100.0;
    }

    public static boolean shouldModify() {
        Module module = ModuleManager.getModuleByName("Velocity");
        return module != null && module.isEnabled();
    }

    public static double getHorizontalMultiplierStatic() {
        Module module = ModuleManager.getModuleByName("Velocity");
        if (module != null && module.isEnabled() && module instanceof Velocity vel) {
            return vel.getHorizontalMultiplier();
        }
        return 1.0;
    }

    public static double getVerticalMultiplierStatic() {
        Module module = ModuleManager.getModuleByName("Velocity");
        if (module != null && module.isEnabled() && module instanceof Velocity vel) {
            return vel.getVerticalMultiplier();
        }
        return 1.0;
    }
}
