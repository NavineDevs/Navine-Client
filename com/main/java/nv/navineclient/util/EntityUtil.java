package nv.navineclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EntityUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    private EntityUtil() {
    }

    public static List<Entity> getEntitiesInRange(double range) {
        if (mc.player == null || mc.level == null || range <= 0) {
            return Collections.emptyList();
        }
        AABB box = mc.player.getBoundingBox().inflate(range);
        List<Entity> found = mc.level.getEntitiesOfClass(Entity.class, box);
        List<Entity> result = new ArrayList<>(found.size());
        for (Entity entity : found) {
            if (entity != mc.player && entity.isAlive()) {
                result.add(entity);
            }
        }
        return result;
    }

    public static List<LivingEntity> getLivingInRange(double range) {
        if (mc.player == null || mc.level == null || range <= 0) {
            return Collections.emptyList();
        }
        AABB box = mc.player.getBoundingBox().inflate(range);
        List<LivingEntity> found = mc.level.getEntitiesOfClass(LivingEntity.class, box);
        List<LivingEntity> result = new ArrayList<>(found.size());
        for (LivingEntity entity : found) {
            if (entity != mc.player && entity.isAlive()) {
                result.add(entity);
            }
        }
        return result;
    }
}
