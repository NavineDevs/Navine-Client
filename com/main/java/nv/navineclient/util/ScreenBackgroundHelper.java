package nv.navineclient.util;



import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.minecraft.client.gui.screens.ChatScreen;

import net.minecraft.client.gui.screens.DeathScreen;

import net.minecraft.client.gui.screens.DisconnectedScreen;

import net.minecraft.client.gui.screens.GenericMessageScreen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import nv.navineclient.ui.ClickGUI;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import nv.navineclient.ui.BigRadarScreen;
import nv.navineclient.ui.CoolButton;

import nv.navineclient.ui.ClickGUI;



public final class ScreenBackgroundHelper {

    private ScreenBackgroundHelper() {

    }



    public static boolean shouldUseNavineBackground(Screen screen, Minecraft minecraft) {

        if (screen instanceof ClickGUI || screen instanceof BigRadarScreen) {

            return false;

        }

        if (screen instanceof AbstractContainerScreen) {

            return false;

        }

        if (screen instanceof ChatScreen) {

            return minecraft.level == null;

        }

        if (minecraft.level != null) {

            return screen instanceof DisconnectedScreen

                    || screen instanceof GenericMessageScreen

                    || screen instanceof DeathScreen;

        }

        return true;

    }



    public static void loadBackground() {

        BackgroundManager.loadExternalBackground();

    }



    public static void renderNavineBackground(GuiGraphicsExtractor context, int width, int height) {

        BackgroundManager.loadExternalBackground();

        if (context == null) {

            return;

        }

        if (BackgroundManager.hasBackground()) {

            BackgroundManager.renderBackground(context, width, height);

        } else {

            context.fill(0, 0, width, height, NavineTheme.SCREEN_BG);

        }

    }



    public static void renderMenuBackground(GuiGraphicsExtractor context, int width, int height) {

        BackgroundManager.loadExternalBackground();

        if (context == null) {

            return;

        }

        if (BackgroundManager.hasBackground()) {

            BackgroundManager.renderBackground(context, width, height);

        } else {

            context.fill(0, 0, width, height, NavineTheme.SCREEN_BG);

        }

    }



    public static void renderTopBar(GuiGraphicsExtractor context, int width) {

        if (context == null) {

            return;

        }

        context.fill(0, 0, width, 28, NavineTheme.TITLE_BAR_BG);

        context.fill(0, 28, width, 29, NavineTheme.TITLE_BAR_LINE);

    }

    public static CoolButton createTopBackButton(Screen parent) {
        return new CoolButton(8, 6, 72, 18, Component.literal("Back"), button -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                ClientAccess.setScreen(mc, parent);
            }
        }, true);
    }

    public static boolean isMenuMusicScreen(Screen screen) {
        if (screen == null) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level != null) {
            return false;
        }
        if (screen instanceof ClickGUI || screen instanceof BigRadarScreen) {
            return false;
        }
        return true;
    }

    public static boolean shouldPlayMenuMusic() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.level == null && ClientAccess.getScreen(mc) != null && isMenuMusicScreen(ClientAccess.getScreen(mc));
    }

    public static boolean shouldStyleNavineButtons(Screen screen, Minecraft minecraft) {
        if (screen == null || minecraft == null) {
            return false;
        }
        if (screen instanceof ClickGUI || screen instanceof BigRadarScreen) {
            return false;
        }
        if (screen instanceof AbstractContainerScreen || screen instanceof ChatScreen) {
            return false;
        }
        return true;
    }

}

