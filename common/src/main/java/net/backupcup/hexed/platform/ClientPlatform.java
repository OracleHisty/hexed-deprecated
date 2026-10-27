package net.backupcup.hexed.platform;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

/** Client-only loader hooks. */
public interface ClientPlatform {
    /** Looks up a standalone model that was registered for loading through the loader's model API. */
    BakedModel getStandaloneModel(ResourceLocation id);
}
