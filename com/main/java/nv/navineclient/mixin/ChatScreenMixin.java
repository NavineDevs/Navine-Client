package nv.navineclient.mixin;

import com.mojang.blaze3d.platform.InputConstants;


import nv.navineclient.util.ClientAccess;
import nv.navineclient.commands.CommandManager;
import nv.navineclient.util.NavineTheme;
import nv.navineclient.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow protected EditBox input;
    @Shadow private boolean closeOnSubmit;

    @Unique private List<String> navine_suggestions = List.of();
    @Unique private int Navine_selectedSuggestion = -1;
    @Unique private int navine_suggestionscroll = 0;
    @Unique private long Navine_lastCommandAt;
    @Unique private static final int Navine_MAX_VISIBLE = 10;
    @Unique private static final int Navine_ROW_H = 12;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, require = 0)
    private void NavineHandleClientCommandKeys(KeyEvent keyInput, CallbackInfoReturnable<Boolean> cir) {
        if (input == null) {
            return;
        }
        String message = input.getValue();
        if (message == null || message.isEmpty()) {
            navine_clearSuggestions();
            return;
        }
        String trimmed = message.trim();
        String prefix = CommandManager.getPrefix();
        if (!trimmed.startsWith(prefix) || !CommandManager.isShowDotCommands()) {
            navine_clearSuggestions();
            return;
        }

        navine_suggestions = CommandManager.getCompletions(trimmed);
        Navine_clampScroll();
        int key = keyInput.key();

        if (key == InputConstants.KEY_TAB) {
            if (!navine_suggestions.isEmpty()) {
                int index = Navine_selectedSuggestion >= 0 ? Navine_selectedSuggestion : 0;
                input.setValue(navine_suggestions.get(index));
                input.moveCursorToEnd(false);
                navine_clearSuggestions();
                cir.setReturnValue(true);
            }
            return;
        }

        if (key == InputConstants.KEY_DOWN && !navine_suggestions.isEmpty()) {
            if (Navine_selectedSuggestion < 0) {
                Navine_selectedSuggestion = 0;
            } else {
                Navine_selectedSuggestion = Math.min(Navine_selectedSuggestion + 1, navine_suggestions.size() - 1);
            }
            Navine_ensureVisible(Navine_selectedSuggestion);
            cir.setReturnValue(true);
            return;
        }

        if (key == InputConstants.KEY_UP && !navine_suggestions.isEmpty()) {
            if (Navine_selectedSuggestion < 0) {
                Navine_selectedSuggestion = 0;
            } else {
                Navine_selectedSuggestion = Math.max(Navine_selectedSuggestion - 1, 0);
            }
            Navine_ensureVisible(Navine_selectedSuggestion);
            cir.setReturnValue(true);
        }
    }

    @Redirect(
            method = "keyPressed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/CommandSuggestions;hasAllowedInput()Z"
            ),
            require = 0
    )
    private boolean NavineAllowClientCommands(CommandSuggestions suggestions) {
        if (input != null) {
            String message = input.getValue().trim();
            if (!message.isEmpty() && message.startsWith(CommandManager.getPrefix())) {
                return true;
            }
        }
        return suggestions.hasAllowedInput();
    }

    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true, require = 0)
    private void onSendMessage(String message, boolean addToHistory, CallbackInfo ci) {
        String trimmed = message.trim();
        if (!trimmed.startsWith(CommandManager.getPrefix())) {
            return;
        }
        NavineExecuteClientCommand(message, addToHistory);
        ci.cancel();
    }

    @Unique
    private void NavineExecuteClientCommand(String message, boolean addToHistory) {
        String trimmed = message.trim();
        String prefix = CommandManager.getPrefix();
        if (!trimmed.startsWith(prefix)) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - Navine_lastCommandAt < 150L) {
            return;
        }
        Navine_lastCommandAt = now;
        if (addToHistory) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gui != null) {
                ClientAccess.getChat(mc).addRecentChat(trimmed);
            }
        }
        CommandManager.handleCommand(trimmed);
        if (closeOnSubmit) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                ClientAccess.setScreen(mc, null);
            }
        } else if (input != null) {
            input.setValue("");
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gui != null) {
                ClientAccess.getChat(mc).resetChatScroll();
            }
        }
        navine_clearSuggestions();
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 0)
    private void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (input == null || !CommandManager.isShowDotCommands()) {
            return;
        }
        String text = input.getValue();
        String prefix = CommandManager.getPrefix();

        if (text != null && text.startsWith(prefix)) {
            navine_suggestions = CommandManager.getCompletions(text.trim());
            Navine_clampScroll();

            if (!navine_suggestions.isEmpty()) {
                Minecraft mc = Minecraft.getInstance();
                int x = input.getX() + 2;
                int total = navine_suggestions.size();
                int visible = Math.min(Navine_MAX_VISIBLE, total);
                int y = input.getY() - 4 - (visible * Navine_ROW_H);

                int maxWidth = 0;
                for (int i = navine_suggestionscroll; i < navine_suggestionscroll + visible; i++) {
                    maxWidth = Math.max(maxWidth, mc.font.width(navine_suggestions.get(i)));
                }
                maxWidth = Math.max(maxWidth, 80);
                maxWidth = Math.min(maxWidth, context.guiWidth() - x - 12);

                int panelH = visible * Navine_ROW_H + 4;
                RenderUtil.drawRoundedRect(context, x - 4, y - 4, maxWidth + 12, panelH + 4, 3, 0xE012161E);
                RenderUtil.drawRoundedOutline(context, x - 4, y - 4, maxWidth + 12, panelH + 4, 3, 1, NavineTheme.BORDER);
                context.fill(x - 4, y - 4, x - 3, y - 4 + panelH + 4, NavineTheme.ACCENT);

                for (int i = 0; i < visible; i++) {
                    int idx = navine_suggestionscroll + i;
                    String suggestion = navine_suggestions.get(idx);
                    int rowY = y + i * Navine_ROW_H;
                    boolean selected = idx == Navine_selectedSuggestion;
                    if (selected) {
                        context.fill(x - 2, rowY - 1, x + maxWidth + 6, rowY + Navine_ROW_H - 1, 0x403EC4CF);
                    }
                    int color = selected ? NavineTheme.ACCENT : NavineTheme.TEXT_SECONDARY;
                    context.text(mc.font, truncate(mc, suggestion, maxWidth), x, rowY, color, false);
                }

                if (total > visible) {
                    String more = (navine_suggestionscroll + 1) + "-" + (navine_suggestionscroll + visible) + "/" + total;
                    context.text(mc.font, more, x + maxWidth - mc.font.width(more), y - 11, NavineTheme.TEXT_DISABLED, false);
                }
            }
        } else {
            navine_clearSuggestions();
        }
    }

    @Unique
    private void navine_clearSuggestions() {
        navine_suggestions = List.of();
        Navine_selectedSuggestion = -1;
        navine_suggestionscroll = 0;
    }

    @Unique
    private void Navine_clampScroll() {
        int total = navine_suggestions.size();
        int maxScroll = Math.max(0, total - Navine_MAX_VISIBLE);
        navine_suggestionscroll = Math.max(0, Math.min(navine_suggestionscroll, maxScroll));
        if (Navine_selectedSuggestion >= total) {
            Navine_selectedSuggestion = total - 1;
        }
    }

    @Unique
    private void Navine_ensureVisible(int index) {
        if (index < navine_suggestionscroll) {
            navine_suggestionscroll = index;
        } else if (index >= navine_suggestionscroll + Navine_MAX_VISIBLE) {
            navine_suggestionscroll = index - Navine_MAX_VISIBLE + 1;
        }
        Navine_clampScroll();
    }

    @Unique
    private static String truncate(Minecraft mc, String text, int maxWidth) {
        if (mc.font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int limit = Math.max(0, maxWidth - mc.font.width(ellipsis));
        while (text.length() > 0 && mc.font.width(text) > limit) {
            text = text.substring(0, text.length() - 1);
        }
        return text + ellipsis;
    }
}
