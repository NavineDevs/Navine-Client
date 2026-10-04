package nv.navineclient.ui;

import nv.navineclient.util.ClientAccess;

import com.mojang.blaze3d.platform.InputConstants;

import nv.navineclient.util.AuthManager;
import nv.navineclient.util.BackgroundManager;
import nv.navineclient.util.ScreenBackgroundHelper;
import nv.navineclient.util.NavineTheme;
import nv.navineclient.util.UiAnim;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public class LoginScreen extends Screen {
    private final Screen parent;
    private EditBox usernameField;
    private String statusMessage = "";
    private int statusColor = -1;
    private boolean loggingIn = false;
    private float screenAnim = 0f;

    public LoginScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        screenAnim = 0f;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        usernameField = new EditBox(this.font, centerX - 150, centerY - 10, 300, 20, Component.literal("Username"));
        usernameField.setHint(Component.literal("insert username"));
        usernameField.setMaxLength(32);
        usernameField.setEditable(true);
        addRenderableWidget(usernameField);
        setInitialFocus(usernameField);

        addRenderableWidget(ScreenBackgroundHelper.createTopBackButton(parent));

        CoolButton loginButton = new CoolButton(centerX - 100, centerY + 20, 200, 20, Component.literal("Login"), button -> attemptLogin(), true);
        loginButton.setRevealOrder(0);
        addRenderableWidget(loginButton);

        CoolButton cancelButton = new CoolButton(centerX - 100, centerY + 45, 200, 20, Component.literal("CANCEL"), button -> {
            if (this.minecraft != null) {
                ClientAccess.setScreen(this.minecraft, this.parent);
            }
        }, true);
        cancelButton.setRevealOrder(1);
        addRenderableWidget(cancelButton);
    }

    private void attemptLogin() {
        if (loggingIn) return;
        String user = usernameField.getValue().trim();

        if (user.isEmpty()) {
            statusMessage = "Username cannot be empty";
            statusColor = 0xFFff5555;
            return;
        }

        loggingIn = true;
        statusMessage = "Verifying...";
        statusColor = 0xFFffaa00;

        new Thread(() -> {
            boolean success = AuthManager.verify(user);
            if (minecraft != null) {
                minecraft.execute(() -> {
                    if (success) {
                        statusMessage = "Login successful!";
                        statusColor = 0xFF55ff55;
                        ClientAccess.setScreen(minecraft, parent);
                    } else {
                        statusMessage = "Invalid username!";
                        statusColor = 0xFFff5555;
                        loggingIn = false;
                    }
                });
            }
        }, "Login-Thread").start();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        screenAnim = UiAnim.approach(screenAnim, 1f, 8f, delta);
        float ease = UiAnim.easeOutCubic(screenAnim);

        if (BackgroundManager.hasBackground()) {
            BackgroundManager.renderBackground(context, this.width, this.height);
        } else {
            context.fillGradient(0, 0, this.width, this.height, 0xFF030306, 0xFF050508);
        }

        context.fill(0, 0, this.width, this.height, UiAnim.scaleAlpha(0x90000000, ease));

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int titleOffset = (int) ((1f - ease) * 10f);

        context.centeredText(this.font, Component.literal("Navine Client Login"), centerX, centerY - 70 + titleOffset, UiAnim.scaleAlpha(NavineTheme.TEXT_PRIMARY, ease));

        if (!statusMessage.isEmpty()) {
            context.centeredText(this.font, Component.literal(statusMessage), centerX, centerY - 35 + titleOffset, UiAnim.scaleAlpha(statusColor, ease));
        }

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(KeyEvent keyInput) {
        if (keyInput.key() == InputConstants.KEY_RETURN || keyInput.key() == InputConstants.KEY_NUMPADENTER) {
            attemptLogin();
            return true;
        }
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
