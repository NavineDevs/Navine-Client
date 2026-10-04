package nv.navineclient.module.combat;

import nv.navineclient.module.Module;
import nv.navineclient.module.ModuleManager;
import nv.navineclient.module.settings.BooleanSetting;
import nv.navineclient.module.settings.NumberSetting;
import nv.navineclient.util.AcBypassUtil;

public class Reach extends Module {
    public static Reach INSTANCE;
    private final NumberSetting reachDist = new NumberSetting("Reach", "Attack reach distance", 4.5, 3.0, 20.0);
    private final NumberSetting blockReach = new NumberSetting("BlockReach", "Block interaction reach", 5.0, 3.0, 20.0);
    private final BooleanSetting creativeOnly = new BooleanSetting("CreativeOnly", "Only in creative mode", false);

    public Reach() {
        super("Reach", "Increases attack and block reach", Category.COMBAT);
        INSTANCE = this;
        addSetting(reachDist);
        addSetting(blockReach);
        addSetting(creativeOnly);
    }

    public boolean isActive() {
        if (!isEnabled()) return false;
        if (creativeOnly.getValue() && mc.player != null && !mc.player.isCreative()) {
            return false;
        }
        return true;
    }

    public double getReachDistance() {
        return AcBypassUtil.clampAttackReach(reachDist.getValue());
    }

    public double getBlockReachDistance() {
        return AcBypassUtil.clampBlockReach(blockReach.getValue());
    }

    public static double getAttackReach() {
        Module module = ModuleManager.getModuleByName("Reach");
        if (module != null && module.isEnabled() && module instanceof Reach reach && reach.isActive()) {
            return reach.getReachDistance();
        }
        return 3.0;
    }

    public static double getBlockReach() {
        Module module = ModuleManager.getModuleByName("Reach");
        if (module != null && module.isEnabled() && module instanceof Reach reach && reach.isActive()) {
            return reach.getBlockReachDistance();
        }
        return 4.5;
    }
}
