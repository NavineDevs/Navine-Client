package nv.navineclient.mixin;

import nv.navineclient.module.misc.PacketLogger;
import nv.navineclient.module.movement.Blink;
import nv.navineclient.module.player.AntiCrash;
import nv.navineclient.module.world.DevMod;
import nv.navineclient.NavineClient;
import nv.navineclient.module.ModuleManager;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ClientConnectionMixin {

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true, require = 0)
    private void navine$onReceivePacket(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        AntiCrash antiCrash = (AntiCrash) ModuleManager.getModuleByName("AntiCrash");
        if (antiCrash != null && antiCrash.isEnabled()) {
            try {
                if (packet == null) {
                    antiCrash.recordPreventedCrash("null-packet");
                    ci.cancel();
                    return;
                }
            } catch (Exception e) {
                antiCrash.recordPreventedCrash("packet-read");
                ci.cancel();
                return;
            }
        }

        try {
            PacketLogger logger = (PacketLogger) ModuleManager.getModuleByName("PacketLogger");
            if (logger != null && logger.isEnabled()) {
                logger.logIncoming(packet);
            }
        } catch (Exception e) {
            NavineClient.LOGGER.warn("PacketLogger failed on incoming packet", e);
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (DevMod.shouldLogPackets()) {
            NavineClient.LOGGER.info("[DevMod] Outgoing: {}", packet.getClass().getSimpleName());
        }

        try {
            PacketLogger logger = (PacketLogger) ModuleManager.getModuleByName("PacketLogger");
            if (logger != null && logger.isEnabled()) {
                logger.logOutgoing(packet);
            }
        } catch (Exception e) {
            NavineClient.LOGGER.warn("PacketLogger failed on outgoing packet", e);
        }

        if (packet instanceof ServerboundMovePlayerPacket movePacket) {
            if (Blink.shouldCancelPacket()) {
                Blink.storePacket(movePacket);
                ci.cancel();
            }
        }
    }
}
