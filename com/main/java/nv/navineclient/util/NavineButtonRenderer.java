package nv.navineclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public final class NavineButtonRenderer {
    private NavineButtonRenderer() {
    }

    public static void render(GuiGraphicsExtractor context, AbstractWidget widget, int textColorOverride) {
        renderMenuStyle(context, widget, textColorOverride, widget.isHoveredOrFocused() ? 1f : 0f, 1f);
    }

    public static void renderMenuStyle(GuiGraphicsExtractor context, AbstractWidget widget, int textColorOverride) {
        renderMenuStyle(context, widget, textColorOverride, widget.isHoveredOrFocused() ? 1f : 0f, 1f);
    }

    public static void renderMenuStyle(GuiGraphicsExtractor context, AbstractWidget widget, int textColorOverride, float hoverFactor) {
        renderMenuStyle(context, widget, textColorOverride, hoverFactor, 1f);
    }

    public static void renderMenuStyle(GuiGraphicsExtractor context, AbstractWidget widget, int textColorOverride, float hoverFactor, float revealAnim) {
        hoverFactor = Math.max(0f, Math.min(1f, hoverFactor));
        revealAnim = Math.max(0f, Math.min(1f, revealAnim));

        int x = widget.getX();
        int y = widget.getY();
        int w = widget.getWidth();
        int h = widget.getHeight();
        boolean active = widget.active;

        int bg = !active
                ? NavineTheme.PANEL_BG
                : UiAnim.lerpColor(NavineTheme.PANEL_BG, NavineTheme.HOVER, hoverFactor);
        RenderUtil.drawRoundedRect(context, x, y, w, h, 2, UiAnim.scaleAlpha(bg, revealAnim));

        int border = !active
                ? NavineTheme.BORDER
                : UiAnim.lerpColor(NavineTheme.BORDER, NavineTheme.ACCENT, hoverFactor * 0.4f);
        RenderUtil.drawRoundedOutline(context, x, y, w, h, 2, 1, UiAnim.scaleAlpha(border, revealAnim));

        int color;
        if (textColorOverride != -1) {
            color = UiAnim.scaleAlpha(textColorOverride, revealAnim);
        } else if (!active) {
            color = UiAnim.scaleAlpha(NavineTheme.TEXT_DISABLED, revealAnim);
        } else {
            color = UiAnim.scaleAlpha(
                    UiAnim.lerpColor(NavineTheme.TEXT_SECONDARY, NavineTheme.TEXT_PRIMARY, hoverFactor),
                    revealAnim
            );
        }

        Component message = widget.getMessage();
        context.centeredText(Minecraft.getInstance().font, message, x + w / 2, y + (h - 8) / 2, color);
    }

    public static void renderPanelButton(GuiGraphicsExtractor context, int x, int y, int w, int h, String label, float hoverFactor, int alpha) {
        hoverFactor = Math.max(0f, Math.min(1f, hoverFactor));
        int bg = UiAnim.lerpColor(NavineTheme.PANEL_BG, NavineTheme.HOVER, hoverFactor);
        RenderUtil.drawRoundedRect(context, x, y, w, h, 2, applyAlpha(bg, alpha));
        int border = UiAnim.lerpColor(NavineTheme.BORDER, NavineTheme.ACCENT, hoverFactor * 0.4f);
        RenderUtil.drawRoundedOutline(context, x, y, w, h, 2, 1, applyAlpha(border, alpha));
        int textBase = UiAnim.lerpColor(NavineTheme.TEXT_SECONDARY, NavineTheme.TEXT_PRIMARY, hoverFactor);
        int textColor = applyAlpha(textBase, alpha);
        int textW = Minecraft.getInstance().font.width(label);
        context.text(Minecraft.getInstance().font, label, x + (w - textW) / 2, y + (h - 8) / 2, textColor, false);
    }

    private static int applyAlpha(int color, int alpha) {
        int a = Math.min(255, Math.max(0, alpha));
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
