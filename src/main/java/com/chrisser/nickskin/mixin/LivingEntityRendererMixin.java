package com.chrisser.nickskin.mixin;

import com.chrisser.nickskin.NickConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    // Vanilla hides your own name tag; show it (with your nick) in third person
    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true, require = 0)
    private void nickskin$showOwnTag(LivingEntity entity, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        if (NickConfig.nick == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (entity == mc.player && !mc.options.getPerspective().isFirstPerson() && MinecraftClient.isHudEnabled()) {
            cir.setReturnValue(true);
        }
    }
}
