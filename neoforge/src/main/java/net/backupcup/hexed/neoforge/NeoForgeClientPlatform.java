package net.backupcup.hexed.neoforge;

import net.backupcup.hexed.platform.ClientPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public class NeoForgeClientPlatform implements ClientPlatform {
    @Override
    public BakedModel getStandaloneModel(ResourceLocation id) {
        return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(id));
    }
}
