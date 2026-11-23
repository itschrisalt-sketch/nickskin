package com.chrisser.nickskin.mixin;

import com.chrisser.nickskin.NickConfig;
import com.chrisser.nickskin.SkinFetcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {
    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void nickskin$swapSkin(CallbackInfoReturnable<SkinTextures> cir) {
        if (NickConfig.skinName == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerListEntry self = (PlayerListEntry) (Object) this;
        if (!self.getProfile().name().equals(mc.getSession().getUsername())) return;
        cir.setReturnValue(SkinFetcher.apply(cir.getReturnValue()));
    }
}
