package net.backupcup.hexed.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.backupcup.hexed.util.AflameBurning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Shows the first-person fire overlay while Aflame burns the player. */
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
    @ModifyExpressionValue(method = "renderScreenEffect",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z"))
    private static boolean hexed$aflameOverlay(boolean original, Minecraft minecraft, PoseStack poseStack) {
        return original || (minecraft.player instanceof AflameBurning aflame && aflame.hexed$isAflameBurning());
    }
}
