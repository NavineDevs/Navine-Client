package nv.navineclient.ui;

import com.mojang.blaze3d.platform.InputConstants;


import nv.navineclient.util.ClientAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import nv.navineclient.util.AuthManager;
import nv.navineclient.util.IRCClient;
import nv.navineclient.util.NavineTheme;
import nv.navineclient.util.RenderUtil;
import nv.navineclient.util.ScreenBackgroundHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class IRCLogScreen extends Screen {
    private final Screen parent;
    private int scroll = 0;
    private int maxScroll = 0;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private EditBox messageField;

    public IRCLogScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(ScreenBackgroundHelper.createTopBackButton(parent));

        int buttonW = 72;
        int buttonH = 18;
        int bottomY = this.height - 28;
        int gap = 6;
        int inputW = Math.min(280, this.width - 240);
        int sendW = 48;
        int startX = 16;

        messageField = new EditBox(this.font, startX, bottomY, inputW, 18, Component.literal("Message"));
        messageField.setMaxLength(256);
        messageField.setHint(Component.literal("Type message..."));
        messageField.setEditable(true);
        addRenderableWidget(messageField);

        int btnX = startX + inputW + gap;
        addRenderableWidget(new CoolButton(btnX, bottomY, sendW, buttonH,
                Component.literal("Send"), button -> sendMessage(), true));
        btnX += sendW + gap;
        addRenderableWidget(new CoolButton(btnX, bottomY, buttonW, buttonH,
                Component.literal("Connect"), button -> {
                    if (!AuthManager.isVerified()) {
                        IRCClient.showStatusMessage("Login required. Use .login <account>");
                        return;
                    }
                    if (!IRCClient.isConnected()) {
                        IRCClient.connect(AuthManager.getMinecraftUsername());
                    }
                }, true));
        btnX += buttonW + gap;
        addRenderableWidget(new CoolButton(btnX, bottomY, buttonW, buttonH,
                Component.literal("Disconnect"), button -> {
                    if (IRCClient.isConnected()) {
                        IRCClient.disconnect();
                    }
                }, true));
        btnX += buttonW + gap;
        addRenderableWidget(new CoolButton(btnX, bottomY, buttonW, buttonH,
                Component.literal("Close"), button -> closeScreen(), true));
    }

    private void sendMessage() {
        if (messageField == null) {
            return;
        }
        String text = messageField.getValue().trim();
        if (text.isEmpty()) {
            return;
        }
        if (!AuthManager.isVerified()) {
            IRCClient.showStatusMessage("Login required first.");
            return;
        }
        if (!IRCClient.isConnected()) {
            IRCClient.showStatusMessage("Not connected. Press Connect.");
            return;
        }
        String username = AuthManager.getMinecraftUsername();
        String rankUser = AuthManager.getIrcRankUser();
        IRCClient.addLogEntry(username, text, IRCClient.LogType.SAY);
        IRCClient.sendMessage(text);
        messageField.setValue("");
        messageField.setFocused(true);
    }

    private void closeScreen() {
        if (this.minecraft != null) {
            ClientAccess.setScreen(this.minecraft, parent);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (scrollY * 14)));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE) {
            closeScreen();
            return true;
        }
        if (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) {
            if (messageField != null && messageField.isFocused()) {
                sendMessage();
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        ScreenBackgroundHelper.renderNavineBackground(context, this.width, this.height);
        ScreenBackgroundHelper.renderTopBar(context, this.width);

        String title = "Navine IRC";
        int titleW = this.font.width(title);
        context.text(this.font, title, (this.width - titleW) / 2, 6, NavineTheme.TEXT_PRIMARY, false);

        String statusText = IRCClient.isConnected() ? "Connected" : IRCClient.getStatus();
        int statusColor = IRCClient.isConnected() ? NavineTheme.SUCCESS : NavineTheme.WARNING;
        String account = AuthManager.isVerified() ? AuthManager.getLoggedInUser() : "not logged in";
        String statusLine = statusText + "  ·  " + account + "  ·  IGN " + AuthManager.getMinecraftUsername();
        int statusW = this.font.width(statusLine);
        context.text(this.font, statusLine, (this.width - statusW) / 2, 18, statusColor, true);

        String hint = ".irc say  ·  .irc dm <user>  ·  .irc help";
        int hintW = this.font.width(hint);
        context.text(this.font, hint, (this.width - hintW) / 2, 28, NavineTheme.TEXT_DISABLED, false);

        int listTop = 40;
        int listBottom = this.height - 52;
        int listHeight = listBottom - listTop;
        int rowHeight = 12;
        List<IRCClient.IrcLogEntry> entries = IRCClient.getLogEntries();

        int contentHeight = entries.isEmpty() ? rowHeight + 8 : entries.size() * rowHeight + 8;
        maxScroll = Math.max(0, contentHeight - listHeight);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        int panelX = 16;
        int panelW = this.width - 32;
        RenderUtil.drawRoundedRect(context, panelX, listTop, panelW, listHeight, 3, NavineTheme.PANEL_BG | 0xD0000000);
        RenderUtil.drawRoundedOutline(context, panelX, listTop, panelW, listHeight, 3, 1, NavineTheme.BORDER);

        context.enableScissor(panelX + 4, listTop + 4, panelX + panelW - 4, listBottom - 4);

        if (entries.isEmpty()) {
            String empty = "No IRC messages yet. Connect after .login with your client account.";
            int emptyW = this.font.width(empty);
            context.text(this.font, empty, panelX + (panelW - emptyW) / 2, listTop + 14, NavineTheme.TEXT_SECONDARY, true);
        } else {
            int y = listTop + 6 - scroll;
            for (IRCClient.IrcLogEntry entry : entries) {
                if (y + rowHeight >= listTop && y <= listBottom) {
                    renderLogEntry(context, entry, panelX + 8, y, panelW - 16);
                }
                y += rowHeight;
            }
        }

        context.disableScissor();

        if (maxScroll > 0) {
            int trackX = panelX + panelW - 6;
            int trackTop = listTop + 4;
            int trackBottom = listBottom - 4;
            int trackH = trackBottom - trackTop;
            context.fill(trackX, trackTop, trackX + 4, trackBottom, NavineTheme.SCROLLBAR_BG);
            int thumbH = Math.max(16, trackH * listHeight / Math.max(1, contentHeight));
            int thumbY = trackTop + (maxScroll == 0 ? 0 : (trackH - thumbH) * scroll / maxScroll);
            context.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, NavineTheme.SCROLLBAR_COLOR);
        }

        for (var child : children()) {
            if (child instanceof net.minecraft.client.gui.components.Renderable renderable) {
                renderable.extractRenderState(context, mouseX, mouseY, delta);
            }
        }
    }

    private void renderLogEntry(GuiGraphicsExtractor context, IRCClient.IrcLogEntry entry, int x, int y, int maxWidth) {
        String time = timeFormat.format(new Date(entry.timestamp));
        context.text(this.font, time, x, y, NavineTheme.TEXT_DISABLED, false);
        int cursorX = x + this.font.width(time) + 4;

        switch (entry.type) {
            case OWNER_ANNOUNCE -> {
                String prefix = "[ANNOUNCE] ";
                context.text(this.font, prefix, cursorX, y, 0xFFCC55FF, false);
                cursorX += this.font.width(prefix);
                String sender = entry.sender.isEmpty() ? "Owner" : entry.sender;
                context.text(this.font, sender + ": ", cursorX, y, 0xFFDD88FF, false);
                cursorX += this.font.width(sender + ": ");
                context.text(this.font, truncate(entry.message, maxWidth - (cursorX - x)), cursorX, y, 0xFFFFFF55, false);
            }
            case DM -> {
                String localUser = AuthManager.getMinecraftUsername();
                String prefix = "[DM] ";
                context.text(this.font, prefix, cursorX, y, 0xFFFF55FF, false);
                cursorX += this.font.width(prefix);
                if (entry.sender.equalsIgnoreCase(localUser) && !entry.recipient.isEmpty()) {
                    String toLine = "to " + entry.recipient + " ";
                    context.text(this.font, toLine, cursorX, y, 0xFFAA88FF, false);
                    cursorX += this.font.width(toLine);
                    context.text(this.font, truncate(entry.message, maxWidth - (cursorX - x)), cursorX, y, NavineTheme.TEXT_PRIMARY, false);
                } else {
                    String fromLine = "from ";
                    context.text(this.font, fromLine, cursorX, y, 0xFFAA88FF, false);
                    cursorX += this.font.width(fromLine);
                    renderTaggedSender(context, entry.sender, cursorX, y);
                    cursorX += this.font.width(formatSenderLine(entry.sender));
                    context.text(this.font, truncate(entry.message, maxWidth - (cursorX - x)), cursorX, y, NavineTheme.TEXT_PRIMARY, false);
                }
            }
            case JOIN, LEAVE -> {
                context.text(this.font, truncate(entry.message, maxWidth - (cursorX - x)), cursorX, y, NavineTheme.TEXT_DISABLED, false);
            }
            case SYSTEM, ERROR -> {
                int color = entry.type == IRCClient.LogType.ERROR ? NavineTheme.DANGER : NavineTheme.TEXT_SECONDARY;
                context.text(this.font, truncate(entry.message, maxWidth - (cursorX - x)), cursorX, y, color, false);
            }
            default -> {
                renderTaggedSender(context, entry.sender, cursorX, y);
                cursorX += this.font.width(formatSenderLine(entry.sender));
                context.text(this.font, truncate(entry.message, maxWidth - (cursorX - x)), cursorX, y, NavineTheme.TEXT_PRIMARY, false);
            }
        }
    }

    private int tagColorFor(String sender) {
        String color = AuthManager.getTagColor(sender);
        if ("§9".equals(color)) {
            return 0xFF5599FF;
        }
        if ("§1".equals(color)) {
            return 0xFF3355AA;
        }
        return NavineTheme.ACCENT;
    }

    private void renderTaggedSender(GuiGraphicsExtractor context, String sender, int x, int y) {
        String tag = AuthManager.getTagForIRC(sender);
        if (tag != null && !tag.isEmpty()) {
            String tagDisplay = tag.replace("[", "").replace("]", "");
            String bracketed = "[" + tagDisplay + "] ";
            context.text(this.font, bracketed, x, y, tagColorFor(sender), false);
            x += this.font.width(bracketed);
        }
        context.text(this.font, sender + ": ", x, y, NavineTheme.TEXT_PRIMARY, false);
    }

    private String formatSenderLine(String sender) {
        String tag = AuthManager.getTagForIRC(sender);
        if (tag != null && !tag.isEmpty()) {
            return "[" + tag.replace("[", "").replace("]", "") + "] " + sender + ": ";
        }
        return sender + ": ";
    }

    private String truncate(String text, int maxWidth) {
        if (text == null) {
            return "";
        }
        if (this.font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int limit = Math.max(0, maxWidth - this.font.width(ellipsis));
        while (text.length() > 0 && this.font.width(text) > limit) {
            text = text.substring(0, text.length() - 1);
        }
        return text + ellipsis;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
