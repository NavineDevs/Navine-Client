package nv.navineclient.module.misc;

import nv.navineclient.module.Module;
import nv.navineclient.module.settings.ModeSetting;
import nv.navineclient.util.ChatUtils;

public class LegitMode extends Module {
    public static LegitMode INSTANCE;

    public enum Profile {
        BALANCED,
        STRICT
    }

    private final ModeSetting profile = new ModeSetting("Profile", "How hard to cap blatant modules", "Balanced", "Balanced", "Strict");

    public LegitMode() {
        super("LegitMode", "Optional AC survival caps — off by default, modules stay blatant", Category.MISC);
        INSTANCE = this;
        addSetting(profile);
    }

    @Override
    public void onEnable() {
        ChatUtils.success("LegitMode on (" + profile.getValue() + ") — softer caps so blatant modules are less likely to insta-ban.");
    }

    public static boolean isActive() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }

    public static Profile getProfile() {
        if (!isActive()) {
            return null;
        }
        return "Strict".equalsIgnoreCase(INSTANCE.profile.getValue()) ? Profile.STRICT : Profile.BALANCED;
    }
}
