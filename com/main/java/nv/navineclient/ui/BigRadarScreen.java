package nv.navineclient.ui;

import nv.navineclient.util.ClientAccess;

import com.mojang.blaze3d.platform.InputConstants;

import nv.navineclient.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import nv.navineclient.module.ModuleManager;
import nv.navineclient.module.render.Radar;
import nv.navineclient.module.render.Trajectories;
import nv.navineclient.util.NavineTheme;
import nv.navineclient.util.ScreenBackgroundHelper;

public class BigRadarScreen extends Screen {
    private Radar radar;
    private int mapSize = 400;
    private int centerX, centerY;
    private double scale = 1.0;
    
    public BigRadarScreen() {
        super(Component.literal("Big Radar"));
        radar = (Radar) ModuleManager.getModuleByName("Radar");
    }
    
    @Override
    protected void init() {
        if (radar == null || !radar.isEnabled() || !radar.isBigRadarEnabled()) {
            this.onClose();
            return;
        }
        
        centerX = width / 2;
        centerY = height / 2;
        
        // Close button
        addRenderableWidget(new CoolButton(width / 2 - 50, height - 30, 100, 20,
            Component.literal("Close"), button -> this.onClose(), true));
    }
    
    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Minecraft client = this.minecraft;
        if (client == null || client.player == null || client.level == null || radar == null) {
            this.onClose();
            return;
        }
        
        // Dark background
        ScreenBackgroundHelper.renderNavineBackground(context, width, height);
        context.fill(0, 0, width, height, 0x90000000);
        
        // Draw radar map
        int mapX = centerX - mapSize / 2;
        int mapY = centerY - mapSize / 2;
        
        // Map background
        context.fill(mapX, mapY, mapX + mapSize, mapY + mapSize, 0xC0101010);
        
        // Grid
        int gridSpacing = mapSize / 20;
        int gridColor = 0x40FFFFFF;
        for (int i = 1; i < 20; i++) {
            context.fill(mapX + i * gridSpacing, mapY, mapX + i * gridSpacing + 1, mapY + mapSize, gridColor);
            context.fill(mapX, mapY + i * gridSpacing, mapX + mapSize, mapY + i * gridSpacing + 1, gridColor);
        }
        
        // Center crosshair
        int centerMapX = mapX + mapSize / 2;
        int centerMapY = mapY + mapSize / 2;
        context.fill(centerMapX - 1, mapY, centerMapX + 1, mapY + mapSize, 0x60FFFFFF);
        context.fill(mapX, centerMapY - 1, mapX + mapSize, centerMapY + 1, 0x60FFFFFF);
        
        // Border with Navine colors
        int borderThickness = 1;
        context.fill(mapX, mapY, mapX + mapSize, mapY + borderThickness, NavineTheme.BORDER);
        context.fill(mapX, mapY + mapSize - borderThickness, mapX + mapSize, mapY + mapSize, NavineTheme.BORDER);
        context.fill(mapX, mapY, mapX + borderThickness, mapY + mapSize, NavineTheme.BORDER);
        context.fill(mapX + mapSize - borderThickness, mapY, mapX + mapSize, mapY + mapSize, NavineTheme.BORDER);

        context.fill(centerMapX - 2, centerMapY - 2, centerMapX + 2, centerMapY + 2, NavineTheme.ACCENT);
        context.fill(centerMapX - 1, centerMapY - 1, centerMapX + 1, centerMapY + 1, NavineTheme.TEXT_PRIMARY);
        
        float yaw = client.player.getYRot();
        double dirX = Math.cos(Math.toRadians(yaw + 90));
        double dirZ = Math.sin(Math.toRadians(yaw + 90));
        int dirEndX = centerMapX + (int)(dirX * 15);
        int dirEndY = centerMapY + (int)(dirZ * 15);
        Trajectories.drawLine(context, centerMapX, centerMapY, dirEndX, dirEndY, NavineTheme.ACCENT);

        String[] directions = {"N", "E", "S", "W"};
        int[] compassX = {centerMapX - 3, mapX + mapSize - 12, centerMapX - 3, mapX + 6};
        int[] compassY = {mapY + 6, centerMapY - 4, mapY + mapSize - 14, centerMapY - 4};
        for (int i = 0; i < 4; i++) {
            context.text(font, directions[i], compassX[i], compassY[i], NavineTheme.TEXT_DISABLED, false);
        }
        
        // Draw entities
        double range = radar.getRange();
        for (Entity entity : EntityUtil.getEntitiesInRange(128.0)) {
            if (!(entity instanceof LivingEntity) || entity == client.player) continue;
            
            double dx = entity.getX() - client.player.getX();
            double dz = entity.getZ() - client.player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            
            if (dist > range) continue;
            
            double angle = Math.atan2(dz, dx) - Math.toRadians(client.player.getYRot() + 90);
            double relX = Math.cos(angle) * (dist / range) * (mapSize / 2 - 10);
            double relZ = Math.sin(angle) * (dist / range) * (mapSize / 2 - 10);
            
            int ex = centerMapX + (int) relX;
            int ey = centerMapY + (int) relZ;
            
            if (ex >= mapX + 5 && ex < mapX + mapSize - 5 && ey >= mapY + 5 && ey < mapY + mapSize - 5) {
                int headSize;
                int color;
                if (entity instanceof Player) {
                    color = NavineTheme.ACCENT;
                    headSize = 3;
                    context.fill(ex - headSize, ey - headSize, ex + headSize, ey + headSize, color);
                } else if (entity instanceof Monster) {
                    color = NavineTheme.DANGER;
                    headSize = 3;
                    context.fill(ex - headSize, ey - headSize, ex + headSize, ey + headSize, color);
                } else if (entity instanceof Animal) {
                    color = NavineTheme.SUCCESS;
                    headSize = 3;
                    context.fill(ex - headSize, ey - headSize, ex + headSize, ey + headSize, color);
                } else {
                    continue;
                }
            }
        }
        
        super.extractRenderState(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent keyInput) {
        int keyCode = keyInput.key();
        if (keyCode == InputConstants.KEY_M || keyCode == InputConstants.KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyInput);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
