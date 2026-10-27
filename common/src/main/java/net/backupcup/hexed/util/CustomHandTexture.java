package net.backupcup.hexed.util;

import net.minecraft.resources.ResourceLocation;

/** Items that render a different model in hand than in the inventory. */
public interface CustomHandTexture {
    /** A model id such as {@code hexed:item/bishop_labrys_hand}. */
    ResourceLocation getHandModel();
}
