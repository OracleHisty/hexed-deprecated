package net.backupcup.hexed.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.backupcup.hexed.platform.Services;
import net.backupcup.hexed.util.CustomHandTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** The tainted weapons use a bigger model everywhere but in GUIs, like the trident and spyglass do. */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private BakedModel hexed$useHandModel(BakedModel model, ItemStack stack, ItemDisplayContext displayContext, boolean leftHand,
                                          PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        if (stack.getItem() instanceof CustomHandTexture item && displayContext != ItemDisplayContext.GUI) {
            BakedModel handModel = Services.Client.PLATFORM.getStandaloneModel(item.getHandModel());
            if (handModel != null) return handModel;
        }
        return model;
    }
}
