package nv.navineclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import nv.navineclient.module.misc.LegitMode;

public final class AcBypassUtil {
    private AcBypassUtil() {
    }

    public static boolean isLegitActive() {
        return LegitMode.isActive();
    }

    public static double jitter(double base, double percent) {
        if (!isLegitActive()) {
            return base;
        }
        double factor = 1.0 + (percent * (Math.sin(System.nanoTime() * 0.0000001) * 0.5 + Math.cos(System.nanoTime() * 0.00000007) * 0.5));
        return base * factor;
    }

    public static float[] smoothRotation(float currentYaw, float currentPitch, float targetYaw, float targetPitch, float maxDelta) {
        if (!isLegitActive()) {
            return new float[]{targetYaw, Math.max(-90, Math.min(90, targetPitch))};
        }
        while (currentYaw > targetYaw + 180) {
            currentYaw -= 360;
        }
        while (currentYaw < targetYaw - 180) {
            currentYaw += 360;
        }
        float yawDiff = targetYaw - currentYaw;
        float pitchDiff = targetPitch - currentPitch;
        float factor = 0.45f;
        float newYaw = currentYaw + Math.max(-maxDelta, Math.min(maxDelta, yawDiff * factor));
        float newPitch = currentPitch + Math.max(-maxDelta, Math.min(maxDelta, pitchDiff * factor));
        return new float[]{newYaw, Math.max(-90, Math.min(90, newPitch))};
    }

    public static void applySmoothLook(LocalPlayer player, float targetYaw, float maxDelta) {
        if (!isLegitActive()) {
            player.setYRot(targetYaw);
            return;
        }
        float[] rotated = smoothRotation(player.getYRot(), player.getXRot(), targetYaw, 0, maxDelta);
        player.setYRot(rotated[0]);
    }

    public static void sendRotationPacket(float yaw, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, mc.player.onGround(), mc.player.horizontalCollision));
    }

    public static boolean canReachEntity(Entity entity, double maxReach) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || entity == null) {
            return false;
        }
        double effectiveReach = clampAttackReach(maxReach);
        if (mc.player.distanceTo(entity) > effectiveReach) {
            return false;
        }
        if (!isLegitActive()) {
            return true;
        }
        Vec3 eyes = mc.player.getEyePosition(1.0f);
        Vec3 target = entity.getBoundingBox().getCenter();
        ClipContext context = new ClipContext(eyes, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player);
        HitResult hit = mc.level.clip(context);
        return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(eyes) >= target.distanceToSqr(eyes) - 0.25;
    }

    public static boolean isInFov(Entity entity, float fovDegrees) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || entity == null) {
            return false;
        }
        Vec3 look = mc.player.getViewVector(1.0f).normalize();
        Vec3 toTarget = entity.getBoundingBox().getCenter().subtract(mc.player.getEyePosition(1.0f)).normalize();
        double dot = look.dot(toTarget);
        double threshold = Math.cos(Math.toRadians(fovDegrees * 0.5));
        return dot >= threshold;
    }

    public static boolean canPlaceAt(Minecraft mc, net.minecraft.core.BlockPos pos) {
        if (mc.level == null || mc.player == null || pos == null) {
            return false;
        }
        net.minecraft.world.level.block.state.BlockState state = mc.level.getBlockState(pos);
        if (!state.isAir() && !state.canBeReplaced()) {
            return false;
        }
        AABB box = new AABB(pos);
        return !box.intersects(mc.player.getBoundingBox());
    }

    public static double clampAttackReach(double requested) {
        boolean creative = Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative();
        if (creative) {
            return Math.min(requested, 6.0);
        }
        if (!isLegitActive()) {
            return requested;
        }
        LegitMode.Profile profile = LegitMode.getProfile();
        return Math.min(requested, profile == LegitMode.Profile.STRICT ? 4.0 : 5.5);
    }

    public static double clampBlockReach(double requested) {
        boolean creative = Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative();
        if (!isLegitActive()) {
            return requested;
        }
        double cap = creative ? 6.0 : 5.5;
        LegitMode.Profile profile = LegitMode.getProfile();
        if (profile == LegitMode.Profile.STRICT) {
            cap = creative ? 5.0 : 4.8;
        }
        return Math.min(requested, cap);
    }

    @Deprecated
    public static double clampReach(double requested, boolean creative) {
        return clampAttackReach(requested);
    }

    public static double clampSpeedMultiplier(double multiplier) {
        if (!isLegitActive()) {
            return multiplier;
        }
        LegitMode.Profile profile = LegitMode.getProfile();
        return Math.min(multiplier, profile == LegitMode.Profile.STRICT ? 1.8 : 2.5);
    }

    public static float clampTimer(float timer) {
        if (!isLegitActive()) {
            return timer;
        }
        LegitMode.Profile profile = LegitMode.getProfile();
        return Math.min(timer, profile == LegitMode.Profile.STRICT ? 1.5f : 2.0f);
    }

    public static float clampStepHeight(float height) {
        if (!isLegitActive()) {
            return height;
        }
        LegitMode.Profile profile = LegitMode.getProfile();
        return Math.min(height, profile == LegitMode.Profile.STRICT ? 1.5f : 2.5f);
    }

    public static double clampCps(double cps) {
        if (!isLegitActive()) {
            return cps;
        }
        LegitMode.Profile profile = LegitMode.getProfile();
        return Math.min(cps, profile == LegitMode.Profile.STRICT ? 18.0 : 25.0);
    }

    public static double minVelocityPercent(double percent) {
        return percent;
    }

    public static double maxHorizontalBps() {
        if (!isLegitActive()) {
            return 100.0;
        }
        LegitMode.Profile profile = LegitMode.getProfile();
        return profile == LegitMode.Profile.STRICT ? 18.0 : 28.0;
    }

    public static double maxTeleportSegment() {
        if (!isLegitActive()) {
            return Double.MAX_VALUE;
        }
        LegitMode.Profile profile = LegitMode.getProfile();
        return profile == LegitMode.Profile.STRICT ? 8.0 : 15.0;
    }

    public static Vec3 clampHorizontalSpeed(Vec3 motion, double maxBps) {
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontal <= maxBps || horizontal <= 0.0001) {
            return motion;
        }
        double scale = maxBps / horizontal;
        return new Vec3(motion.x * scale, motion.y, motion.z * scale);
    }

    public static Vec3 blendHorizontalMotion(Vec3 current, Vec3 target, double blend) {
        return new Vec3(
                current.x + (target.x - current.x) * blend,
                current.y,
                current.z + (target.z - current.z) * blend
        );
    }

    public static int legitMiningDelayTicks() {
        return 0;
    }

    public static boolean allowVanillaFlight() {
        return true;
    }

    public static boolean allowPacketCriticals() {
        return true;
    }

    public static int legitBlinkReleaseRate(int requested) {
        if (!isLegitActive()) {
            return requested;
        }
        return Math.min(requested, LegitMode.getProfile() == LegitMode.Profile.STRICT ? 4 : 8);
    }

    public static int legitBlinkMaxPackets(int requested) {
        if (!isLegitActive()) {
            return requested;
        }
        return Math.min(requested, LegitMode.getProfile() == LegitMode.Profile.STRICT ? 120 : 200);
    }
}
