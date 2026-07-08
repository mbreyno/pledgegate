package com.michaelreynolds.pledgegate.mixin;

import com.michaelreynolds.pledgegate.DialogManager;
import com.michaelreynolds.pledgegate.PledgeGate;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
	/**
	 * Intercepts dialog button clicks for the pledgegate namespace. Injecting at
	 * the MinecraftServer.handleCustomClickAction call site means the vanilla
	 * thread check has already run, so we are on the server thread here.
	 */
	@Inject(
			method = "handleCustomClickAction",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/MinecraftServer;handleCustomClickAction(Lnet/minecraft/resources/Identifier;Ljava/util/Optional;)V"
			),
			cancellable = true
	)
	private void pledgegate$handleCustomClickAction(ServerboundCustomClickActionPacket packet, CallbackInfo ci) {
		if (!PledgeGate.MOD_ID.equals(packet.id().getNamespace())) {
			return;
		}
		if ((Object) this instanceof ServerGamePacketListenerImpl gameListener) {
			DialogManager.handleClick(gameListener.player, packet.id(), packet.payload());
		}
		ci.cancel();
	}
}
