package net.backupcup.hexed.client.render;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.entity.BlazingSkullEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BlazingSkullRenderer extends MobRenderer<BlazingSkullEntity, BlazingSkullModel> {
    private static final ResourceLocation TEXTURE = Hexed.id("textures/entity/blazing_skull.png");

    public BlazingSkullRenderer(EntityRendererProvider.Context context) {
        super(context, new BlazingSkullModel(context.bakeLayer(BlazingSkullModel.LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(BlazingSkullEntity entity) {
        return TEXTURE;
    }
}
