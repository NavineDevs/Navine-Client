package nv.navineclient.module.player;

import nv.navineclient.util.ClientAccess;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import nv.navineclient.module.Module;
import nv.navineclient.module.settings.BooleanSetting;

public class InventoryWalk extends Module {
    private final BooleanSetting sneak = new BooleanSetting("Sneak", "Allow sneaking in inventory", true);
    private final BooleanSetting jump = new BooleanSetting("Jump", "Allow jumping in inventory", true);
    private final BooleanSetting sprint = new BooleanSetting("Sprint", "Allow sprinting in inventory", true);

    public InventoryWalk() {
        super("InventoryWalk", "Move while in inventory", Category.PLAYER);
        addSetting(sneak);
        addSetting(jump);
        addSetting(sprint);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null || mc.getWindow() == null) return;
        if (!(ClientAccess.getScreen(mc) instanceof AbstractContainerScreen)) return;

        long window = mc.getWindow().handle();
        mc.options.keyUp.setDown(ClientAccess.isKeyDown(InputConstants.KEY_W));
        mc.options.keyDown.setDown(ClientAccess.isKeyDown(InputConstants.KEY_S));
        mc.options.keyLeft.setDown(ClientAccess.isKeyDown(InputConstants.KEY_A));
        mc.options.keyRight.setDown(ClientAccess.isKeyDown(InputConstants.KEY_D));

        if (jump.getValue()) {
            mc.options.keyJump.setDown(ClientAccess.isKeyDown(InputConstants.KEY_SPACE));
        }
        if (sneak.getValue()) {
            mc.options.keyShift.setDown(ClientAccess.isKeyDown(InputConstants.KEY_LSHIFT)
                || ClientAccess.isKeyDown(InputConstants.KEY_RSHIFT));
        }
        if (sprint.getValue()) {
            mc.options.keySprint.setDown(ClientAccess.isKeyDown(InputConstants.KEY_LCONTROL)
                || ClientAccess.isKeyDown(InputConstants.KEY_RCONTROL));
        }
    }
}
