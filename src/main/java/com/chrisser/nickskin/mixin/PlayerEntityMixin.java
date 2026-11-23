package com.chrisser.nickskin.mixin;

import com.chrisser.nickskin.NickConfig;
import com.chrisser.nickskin.NickUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    // Name shown above your head
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true, require = 0)
    private void nickskin$headName(CallbackInfoReturnable<Text> cir) {
        if (NickConfig.nick == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || (Object) this != mc.player) return;
        cir.setReturnValue(NickUtil.replaceName(cir.getReturnValue(), mc.getSession().getUsername(), NickConfig.nick));
    }
}
