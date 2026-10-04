package nv.navineclient.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ClientAccess {
    private static Method minecraftSetScreen;
    private static Method minecraftSetScreenAndShow;
    private static Method guiSetScreen;
    private static Method guiScreen;
    private static Field minecraftScreen;
    private static Method guiGetChat;
    private static Method hudGetChat;
    private static Field guiHud;
    private static Method gameRendererMainCamera;
    private static Method gameRendererGetMainCamera;
    private static Method minecraftMainRenderTarget;
    private static Method minecraftGetMainRenderTarget;
    private static Method gameRendererMainRenderTarget;
    private static Method minecraftGetDebugOverlay;
    private static Method guiGetDebugOverlay;
    private static Method hudGetDebugOverlay;
    private static Method hudExtractDeferredSubtitles;
    private static Method guiExtractDeferredSubtitles;
    private static Method renderSystemModelViewMatrix;
    private static Method inputIsKeyDownWindow;
    private static Method inputIsKeyDown;
    private static Method swingHand;
    private static Method swingHandAnim;
    private static Method dropPredicted;
    private static Method dropSimple;
    private static Field hurtMarked;
    private static Field syncVelocity;
    private static Object swingAnimationDefault;
    private static Object predictionPredicted;
    private static boolean resolved;

    private ClientAccess() {
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        Minecraft mc = Minecraft.getInstance();
        Class<?> mcClass = Minecraft.class;
        try {
            minecraftScreen = mcClass.getField("screen");
        } catch (Throwable ignored) {
        }
        try {
            minecraftSetScreen = mcClass.getMethod("setScreen", Screen.class);
        } catch (Throwable ignored) {
        }
        try {
            minecraftSetScreenAndShow = mcClass.getMethod("setScreenAndShow", Screen.class);
        } catch (Throwable ignored) {
        }
        try {
            Object gui = mc.gui;
            Class<?> guiClass = gui.getClass();
            try {
                guiScreen = guiClass.getMethod("screen");
            } catch (Throwable ignored) {
            }
            try {
                guiSetScreen = guiClass.getMethod("setScreen", Screen.class);
            } catch (Throwable ignored) {
            }
            try {
                guiGetChat = guiClass.getMethod("getChat");
            } catch (Throwable ignored) {
            }
            try {
                guiGetDebugOverlay = guiClass.getMethod("getDebugOverlay");
            } catch (Throwable ignored) {
            }
            try {
                guiExtractDeferredSubtitles = guiClass.getMethod("extractDeferredSubtitles");
            } catch (Throwable ignored) {
            }
            try {
                guiHud = guiClass.getField("hud");
            } catch (Throwable ignored) {
            }
            if (guiHud != null) {
                Object hud = guiHud.get(gui);
                Class<?> hudClass = hud.getClass();
                try {
                    hudGetChat = hudClass.getMethod("getChat");
                } catch (Throwable ignored) {
                }
                try {
                    hudGetDebugOverlay = hudClass.getMethod("getDebugOverlay");
                } catch (Throwable ignored) {
                }
                try {
                    hudExtractDeferredSubtitles = hudClass.getMethod("extractDeferredSubtitles");
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            Class<?> gr = mc.gameRenderer.getClass();
            try {
                gameRendererMainCamera = gr.getMethod("mainCamera");
            } catch (Throwable ignored) {
            }
            try {
                gameRendererGetMainCamera = gr.getMethod("getMainCamera");
            } catch (Throwable ignored) {
            }
            try {
                gameRendererMainRenderTarget = gr.getMethod("mainRenderTarget");
            } catch (Throwable ignored) {
            }
        } catch (Throwable ignored) {
        }
        try {
            try {
                minecraftMainRenderTarget = mcClass.getMethod("mainRenderTarget");
            } catch (Throwable ignored) {
            }
            try {
                minecraftGetMainRenderTarget = mcClass.getMethod("getMainRenderTarget");
            } catch (Throwable ignored) {
            }
        } catch (Throwable ignored) {
        }
        try {
            minecraftGetDebugOverlay = mcClass.getMethod("getDebugOverlay");
        } catch (Throwable ignored) {
        }
        try {
            Class<?> rs = Class.forName("com.mojang.blaze3d.systems.RenderSystem");
            try {
                renderSystemModelViewMatrix = rs.getMethod("getModelViewMatrix");
            } catch (Throwable ignored) {
            }
            try {
                if (renderSystemModelViewMatrix == null) {
                    renderSystemModelViewMatrix = rs.getMethod("getModelViewStack");
                }
            } catch (Throwable ignored) {
            }
        } catch (Throwable ignored) {
        }
        try {
            inputIsKeyDown = InputConstants.class.getMethod("isKeyDown", int.class);
        } catch (Throwable ignored) {
        }
        try {
            inputIsKeyDownWindow = InputConstants.class.getMethod("isKeyDown", Window.class, int.class);
        } catch (Throwable ignored) {
        }
        try {
            swingHand = LivingEntity.class.getMethod("swing", InteractionHand.class);
        } catch (Throwable ignored) {
        }
        try {
            Class<?> animClass = Class.forName("net.minecraft.world.item.component.SwingAnimation");
            swingAnimationDefault = animClass.getField("DEFAULT").get(null);
            swingHandAnim = LivingEntity.class.getMethod("swing", InteractionHand.class, animClass, boolean.class);
        } catch (Throwable ignored) {
        }
        try {
            Class<?> predictionClass = Class.forName("net.minecraft.util.Prediction");
            @SuppressWarnings({"unchecked", "rawtypes"})
            Enum predicted = Enum.valueOf((Class) predictionClass, "PREDICTED");
            predictionPredicted = predicted;
            dropPredicted = LivingEntity.class.getMethod("drop", ItemStack.class, boolean.class, predictionClass);
        } catch (Throwable ignored) {
        }
        try {
            dropSimple = LivingEntity.class.getMethod("drop", ItemStack.class, boolean.class);
        } catch (Throwable ignored) {
        }
        try {
            hurtMarked = Entity.class.getField("hurtMarked");
        } catch (Throwable ignored) {
        }
        try {
            syncVelocity = Entity.class.getField("syncVelocity");
        } catch (Throwable ignored) {
        }
    }

    public static Screen getScreen() {
        return getScreen(Minecraft.getInstance());
    }

    public static Screen getScreen(Minecraft mc) {
        if (mc == null) {
            return null;
        }
        resolve();
        try {
            if (guiScreen != null && mc.gui != null) {
                return (Screen) guiScreen.invoke(mc.gui);
            }
            if (minecraftScreen != null) {
                return (Screen) minecraftScreen.get(mc);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static void setScreen(Screen screen) {
        setScreen(Minecraft.getInstance(), screen);
    }

    public static void setScreen(Minecraft mc, Screen screen) {
        if (mc == null) {
            return;
        }
        resolve();
        try {
            if (minecraftSetScreenAndShow != null) {
                minecraftSetScreenAndShow.invoke(mc, screen);
                return;
            }
            if (minecraftSetScreen != null) {
                minecraftSetScreen.invoke(mc, screen);
                return;
            }
            if (guiSetScreen != null && mc.gui != null) {
                guiSetScreen.invoke(mc.gui, screen);
            }
        } catch (Throwable ignored) {
        }
    }

    public static ChatComponent getChat() {
        return getChat(Minecraft.getInstance());
    }

    public static ChatComponent getChat(Minecraft mc) {
        if (mc == null || mc.gui == null) {
            return null;
        }
        resolve();
        try {
            if (guiGetChat != null) {
                return (ChatComponent) guiGetChat.invoke(mc.gui);
            }
            if (hudGetChat != null && guiHud != null) {
                Object hud = guiHud.get(mc.gui);
                return (ChatComponent) hudGetChat.invoke(hud);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static DebugScreenOverlay getDebugOverlay(Minecraft mc) {
        if (mc == null) {
            return null;
        }
        resolve();
        try {
            if (minecraftGetDebugOverlay != null) {
                return (DebugScreenOverlay) minecraftGetDebugOverlay.invoke(mc);
            }
            if (guiGetDebugOverlay != null && mc.gui != null) {
                return (DebugScreenOverlay) guiGetDebugOverlay.invoke(mc.gui);
            }
            if (hudGetDebugOverlay != null && guiHud != null && mc.gui != null) {
                Object hud = guiHud.get(mc.gui);
                return (DebugScreenOverlay) hudGetDebugOverlay.invoke(hud);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static void extractDeferredSubtitles(Minecraft mc) {
        if (mc == null || mc.gui == null) {
            return;
        }
        resolve();
        try {
            if (guiExtractDeferredSubtitles != null) {
                guiExtractDeferredSubtitles.invoke(mc.gui);
                return;
            }
            if (hudExtractDeferredSubtitles != null && guiHud != null) {
                Object hud = guiHud.get(mc.gui);
                hudExtractDeferredSubtitles.invoke(hud);
            }
        } catch (Throwable ignored) {
        }
    }

    public static Camera getMainCamera(Minecraft mc) {
        if (mc == null || mc.gameRenderer == null) {
            return null;
        }
        resolve();
        try {
            if (gameRendererMainCamera != null) {
                return (Camera) gameRendererMainCamera.invoke(mc.gameRenderer);
            }
            if (gameRendererGetMainCamera != null) {
                return (Camera) gameRendererGetMainCamera.invoke(mc.gameRenderer);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static RenderTarget getMainRenderTarget(Minecraft mc) {
        if (mc == null) {
            return null;
        }
        resolve();
        try {
            if (minecraftMainRenderTarget != null) {
                return (RenderTarget) minecraftMainRenderTarget.invoke(mc);
            }
            if (minecraftGetMainRenderTarget != null) {
                return (RenderTarget) minecraftGetMainRenderTarget.invoke(mc);
            }
            if (gameRendererMainRenderTarget != null && mc.gameRenderer != null) {
                return (RenderTarget) gameRendererMainRenderTarget.invoke(mc.gameRenderer);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static boolean isKeyDown(int key) {
        resolve();
        Minecraft mc = Minecraft.getInstance();
        try {
            if (inputIsKeyDown != null) {
                return (Boolean) inputIsKeyDown.invoke(null, key);
            }
            if (inputIsKeyDownWindow != null && mc != null && mc.getWindow() != null) {
                return (Boolean) inputIsKeyDownWindow.invoke(null, mc.getWindow(), key);
            }
        } catch (Throwable ignored) {
        }
        try {
            Class<?> glfw = Class.forName("org.lwjgl.glfw.GLFW");
            Method getKey = glfw.getMethod("glfwGetKey", long.class, int.class);
            Field press = glfw.getField("GLFW_PRESS");
            long handle = mc.getWindow().handle();
            return ((Integer) getKey.invoke(null, handle, key)) == ((Integer) press.get(null));
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static boolean isMouseButtonDown(int button) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.mouseHandler == null) {
            return false;
        }
        try {
            if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                return mc.mouseHandler.isLeftPressed();
            }
            if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                return mc.mouseHandler.isRightPressed();
            }
            if (button == InputConstants.MOUSE_BUTTON_MIDDLE) {
                return mc.mouseHandler.isMiddlePressed();
            }
        } catch (Throwable ignored) {
        }
        return isKeyDown(button);
    }

    public static void swing(LivingEntity entity, InteractionHand hand) {
        if (entity == null || hand == null) {
            return;
        }
        resolve();
        try {
            if (swingHandAnim != null && swingAnimationDefault != null) {
                swingHandAnim.invoke(entity, hand, swingAnimationDefault, true);
                return;
            }
            if (swingHand != null) {
                swingHand.invoke(entity, hand);
            }
        } catch (Throwable ignored) {
        }
    }

    public static ItemEntity drop(LivingEntity entity, ItemStack stack, boolean dropAround) {
        if (entity == null || stack == null) {
            return null;
        }
        resolve();
        try {
            if (dropPredicted != null && predictionPredicted != null) {
                return (ItemEntity) dropPredicted.invoke(entity, stack, dropAround, predictionPredicted);
            }
            if (dropSimple != null) {
                return (ItemEntity) dropSimple.invoke(entity, stack, dropAround);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static void markVelocityDirty(Entity entity) {
        if (entity == null) {
            return;
        }
        resolve();
        try {
            if (syncVelocity != null) {
                syncVelocity.setBoolean(entity, true);
                return;
            }
            if (hurtMarked != null) {
                hurtMarked.setBoolean(entity, true);
            }
        } catch (Throwable ignored) {
        }
    }

    public static String keyName(int key) {
        try {
            return InputConstants.Type.KEYBOARD.getOrCreate(key).getDisplayName().getString();
        } catch (Throwable ignored) {
        }
        try {
            Class<?> glfw = Class.forName("org.lwjgl.glfw.GLFW");
            Method getName = glfw.getMethod("glfwGetKeyName", int.class, int.class);
            Object name = getName.invoke(null, key, 0);
            if (name != null) {
                return name.toString().toUpperCase();
            }
        } catch (Throwable ignored) {
        }
        return "KEY" + key;
    }

    public static Vec3 blockCenter(BlockPos pos) {
        if (pos == null) {
            return Vec3.ZERO;
        }
        try {
            Method m = BlockPos.class.getMethod("getCenter");
            Object result = m.invoke(pos);
            if (result instanceof Vec3 vec) {
                return vec;
            }
        } catch (Throwable ignored) {
        }
        return Vec3.atCenterOf(pos);
    }

    public static Matrix4f modelViewMatrix() {
        resolve();
        try {
            if (renderSystemModelViewMatrix != null) {
                Object result = renderSystemModelViewMatrix.invoke(null);
                if (result instanceof Matrix4f matrix4f) {
                    return matrix4f;
                }
            }
        } catch (Throwable ignored) {
        }
        return new Matrix4f();
    }

    public static boolean hasScreenOpen(Minecraft mc) {
        return getScreen(mc) != null;
    }

    public static void reloadChunks(Minecraft mc) {
        if (mc == null || mc.levelRenderer == null) {
            return;
        }
        try {
            Method allChanged = mc.levelRenderer.getClass().getMethod("allChanged");
            allChanged.invoke(mc.levelRenderer);
            return;
        } catch (Throwable ignored) {
        }
        try {
            Method reset = mc.levelRenderer.getClass().getMethod("resetLevelRenderData");
            reset.invoke(mc.levelRenderer);
        } catch (Throwable ignored) {
        }
    }

    public static String supportedRange() {
        return "1.21.11-26.3";
    }
}
