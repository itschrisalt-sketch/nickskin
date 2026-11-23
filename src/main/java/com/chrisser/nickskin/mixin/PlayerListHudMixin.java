package com.chrisser.nickskin.mixin;

import com.chrisser.nickskin.NickConfig;
import com.chrisser.nickskin.NickUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    // Name shown in the tab list
    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true, require = 0)
    private void nickskin$tabName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (NickConfig.nick == null) return;
        String real = MinecraftClient.getInstance().getSession().getUsername();
        if (!entry.getProfile().name().equals(real)) return;
        cir.setReturnValue(NickUtil.replaceName(cir.getReturnValue(), real, NickConfig.nick));
    }
}
