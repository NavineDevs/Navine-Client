package nv.navineclient.module.movement;

import nv.navineclient.module.Module;
import nv.navineclient.module.settings.ModeSetting;
import nv.navineclient.module.settings.NumberSetting;
import nv.navineclient.module.settings.BooleanSetting;
import net.minecraft.world.phys.Vec3;
import nv.navineclient.util.AcBypassUtil;

public class Speed extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Speed mode", "Vanilla", "Vanilla", "BHop", "Strafe");
    private final NumberSetting speedMult = new NumberSetting("Speed", "Speed multiplier", 1.5, 1.0, 5.0);
    private final BooleanSetting gradual = new BooleanSetting("Gradual", "Gradual acceleration", true);

    private double currentSpeed = 0.0;
    private int speedTickCounter = 0;

    public Speed() {
        super("Speed", "Move faster", Category.MOVEMENT);
        addSetting(mode);
        addSetting(speedMult);
        addSetting(gradual);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        
        speedTickCounter++;

        String m = mode.getValue();
        double s = AcBypassUtil.clampSpeedMultiplier(AcBypassUtil.jitter(speedMult.getValue(), 0.02));
        
        boolean moving = mc.options.keyUp.isDown() || mc.options.keyDown.isDown() ||
                        mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();
        
        if (!moving) {
            // Gradually slow down when not moving
            if (gradual.getValue()) {
                currentSpeed *= 0.9;
            } else {
                currentSpeed = 0;
            }
            return;
        }
        
        float yaw = mc.player.getYRot();
        double baseSpeed = s * 0.3;
        
        // Gradual acceleration for more realistic movement
        if (gradual.getValue()) {
            double targetSpeed = baseSpeed;
            currentSpeed += (targetSpeed - currentSpeed) * 0.3; // Smooth acceleration
            baseSpeed = currentSpeed;
        }
        
        double motionX = 0;
        double motionZ = 0;
        
        if (mc.options.keyUp.isDown()) {
            motionX -= Math.sin(Math.toRadians(yaw)) * baseSpeed;
            motionZ += Math.cos(Math.toRadians(yaw)) * baseSpeed;
        }
        if (mc.options.keyDown.isDown()) {
            motionX += Math.sin(Math.toRadians(yaw)) * baseSpeed;
            motionZ -= Math.cos(Math.toRadians(yaw)) * baseSpeed;
        }
        if (mc.options.keyLeft.isDown()) {
            motionX += Math.cos(Math.toRadians(yaw)) * baseSpeed;
            motionZ += Math.sin(Math.toRadians(yaw)) * baseSpeed;
        }
        if (mc.options.keyRight.isDown()) {
            motionX -= Math.cos(Math.toRadians(yaw)) * baseSpeed;
            motionZ -= Math.sin(Math.toRadians(yaw)) * baseSpeed;
        }
        
        if (m.equals("Vanilla")) {
            if (mc.player.onGround() || !gradual.getValue()) {
                Vec3 motion = new Vec3(motionX, mc.player.getDeltaMovement().y, motionZ);
                motion = AcBypassUtil.clampHorizontalSpeed(motion, AcBypassUtil.maxHorizontalBps() / 20.0);
                mc.player.setDeltaMovement(motion);
            } else {
                // Air movement - more subtle
                double airSpeed = baseSpeed * 0.5;
                mc.player.setDeltaMovement(
                    mc.player.getDeltaMovement().x * 0.9 + motionX * 0.1,
                    mc.player.getDeltaMovement().y,
                    mc.player.getDeltaMovement().z * 0.9 + motionZ * 0.1
                );
            }
        } else if (m.equals("BHop")) {
            if (mc.player.onGround()) {
                mc.player.setDeltaMovement(motionX, mc.player.getDeltaMovement().y, motionZ);
                mc.player.jumpFromGround();
            } else {
                // Air control - subtle
                mc.player.setDeltaMovement(
                    mc.player.getDeltaMovement().x * 0.95 + motionX * 0.05,
                    mc.player.getDeltaMovement().y,
                    mc.player.getDeltaMovement().z * 0.95 + motionZ * 0.05
                );
            }
        } else if (m.equals("Strafe")) {
            double currentSpeed = Math.sqrt(mc.player.getDeltaMovement().x * mc.player.getDeltaMovement().x + mc.player.getDeltaMovement().z * mc.player.getDeltaMovement().z);
            double targetSpeed = Math.sqrt(motionX * motionX + motionZ * motionZ);
            if (targetSpeed > currentSpeed) {
                // Gradual speed increase
                double speedIncrease = (targetSpeed - currentSpeed) * 0.2;
                mc.player.setDeltaMovement(
                    mc.player.getDeltaMovement().x * 0.9 + motionX * speedIncrease,
                    mc.player.getDeltaMovement().y,
                    mc.player.getDeltaMovement().z * 0.9 + motionZ * speedIncrease
                );
            }
            if (mc.player.onGround()) {
                mc.player.jumpFromGround();
            }
        }
    }
    
    @Override
    public void onDisable() {
        currentSpeed = 0.0;
    }
}
