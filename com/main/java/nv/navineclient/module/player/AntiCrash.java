package nv.navineclient.module.player;

import nv.navineclient.util.ClientAccess;

import nv.navineclient.module.Module;
import nv.navineclient.module.settings.BooleanSetting;
import nv.navineclient.util.ChatUtils;

public class AntiCrash extends Module {
    private static final int MAX_CHAT_LENGTH = 4096;
    private static final long FREEZE_ALERT_MS = 5000L;

    private final BooleanSetting protectSelf = new BooleanSetting("ProtectSelf", "Protect your client from self-crash packets", true);
    private final BooleanSetting preventGameFreeze = new BooleanSetting("PreventGameFreeze", "Alert when the client stops ticking", true);
    private final BooleanSetting preventPacketCrash = new BooleanSetting("PreventPacketCrash", "Block oversized or malformed chat packets", true);
    private final BooleanSetting preventEntityCrash = new BooleanSetting("PreventEntityCrash", "Reset invalid entity positions", true);
    private final BooleanSetting preventRenderCrash = new BooleanSetting("PreventRenderCrash", "Skip broken entity rendering", true);
    private final BooleanSetting alertUser = new BooleanSetting("AlertUser", "Alert on prevention", true);

    private int preventedCrashes;
    private long lastAlert;
    private long lastTickTime = System.currentTimeMillis();

    public AntiCrash() {
        super("AntiCrash", "Protect against incoming crashes while allowing outbound tools", Category.PLAYER);
        addSetting(protectSelf);
        addSetting(preventGameFreeze);
        addSetting(preventPacketCrash);
        addSetting(preventEntityCrash);
        addSetting(preventRenderCrash);
        addSetting(alertUser);
    }

    @Override
    public void onTick() {
        long now = System.currentTimeMillis();
        if (preventGameFreeze.getValue() && mc.player != null && ClientAccess.getScreen(mc) == null) {
            long delta = now - lastTickTime;
            if (delta > FREEZE_ALERT_MS && now - lastAlert > FREEZE_ALERT_MS) {
                recordPreventedCrash("client-freeze");
            }
        }
        lastTickTime = now;
    }

    public boolean shouldBlockChatMessage(String content) {
        if (!preventPacketCrash.getValue() || content == null) {
            return false;
        }
        return content.length() > MAX_CHAT_LENGTH;
    }

    public void recordPreventedCrash(String crashType) {
        preventedCrashes++;

        if (alertUser.getValue() && System.currentTimeMillis() - lastAlert > 1000) {
            ChatUtils.message("§6AntiCrash: Prevented " + crashType + " crash");
            lastAlert = System.currentTimeMillis();
        }
    }

    public boolean shouldProtectSelf() {
        return protectSelf.getValue();
    }

    public boolean shouldPreventGameFreeze() {
        return preventGameFreeze.getValue();
    }

    public boolean shouldPreventPacketCrash() {
        return preventPacketCrash.getValue();
    }

    public boolean shouldPreventEntityCrash() {
        return preventEntityCrash.getValue();
    }

    public boolean shouldPreventRenderCrash() {
        return preventRenderCrash.getValue();
    }

    public int getPreventedCrashCount() {
        return preventedCrashes;
    }
}
