package com.chrisser.nickskin.mixin;

import com.chrisser.nickskin.NickConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {
    @Inject(method = "getSkinTextures", at = @At("HEAD"), cancellable = true)
    private void nickskin$swapSkin(CallbackInfoReturnable<SkinTextures> cir) {
        if (NickConfig.skinName == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler == null) return;

        PlayerListEntry self = (PlayerListEntry) (Object) this;
        if (!self.getProfile().name().equals(mc.getSession().getUsername())) return;

        PlayerListEntry other = handler.getPlayerListEntry(NickConfig.skinName);
        if (other != null && other != self) cir.setReturnValue(other.getSkinTextures());
    }
}
