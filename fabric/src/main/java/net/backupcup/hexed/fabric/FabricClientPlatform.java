package net.backupcup.hexed.fabric;

import net.backupcup.hexed.platform.ClientPlatform;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public class FabricClientPlatform implements ClientPlatform {
    @Override
    public BakedModel getStandaloneModel(ResourceLocation id) {
        return ((FabricBakedModelManager) Minecraft.getInstance().getModelManager()).getModel(id);
    }
}
