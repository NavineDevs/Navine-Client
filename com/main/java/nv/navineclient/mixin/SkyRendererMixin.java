package nv.navineclient.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import nv.navineclient.module.ModuleManager;
import nv.navineclient.module.world.CustomSky;
import nv.navineclient.util.CustomSkyRenderer;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
    @Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true, require = 0)
    private void navine$renderImageSky(CallbackInfo ci) {
        CustomSky customSky = (CustomSky) ModuleManager.getModuleByName("CustomSky");
        if (customSky != null && customSky.isEnabled() && customSky.isImageMode() && customSky.getCustomSkyTexture() != null) {
            CustomSkyRenderer.renderCameraSky(customSky);
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"), require = 0)
    private void navine$customSky(
        ClientLevel level, float partialTicks, Camera camera, SkyRenderState state, CallbackInfo ci
    ) {
        CustomSky customSky = (CustomSky) ModuleManager.getModuleByName("CustomSky");
        if (customSky == null || !customSky.isEnabled() || state.skybox == DimensionType.Skybox.NONE) {
            return;
        }
        if (state.skybox != DimensionType.Skybox.END) {
            int rgb = customSky.getSkyColor();
            state.skyColor = new Vector3f(
                    ((rgb >> 16) & 0xFF) / 255.0F,
                    ((rgb >> 8) & 0xFF) / 255.0F,
                    (rgb & 0xFF) / 255.0F
            );
            if (customSky.shouldDisableVoid() || customSky.isImageMode()) {
                state.shouldRenderDarkDisc = false;
            }
        }
    }

    @Inject(method = "renderSunMoonAndStars", at = @At("HEAD"), cancellable = true, require = 0)
    private void navine$hideCelestial(CallbackInfo ci) {
        CustomSky customSky = (CustomSky) ModuleManager.getModuleByName("CustomSky");
        if (customSky != null && customSky.isEnabled() && (customSky.isImageMode() || customSky.shouldDisableVoid())) {
            ci.cancel();
        }
    }
}
