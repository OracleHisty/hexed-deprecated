package net.backupcup.hexed.datagen

import net.backupcup.hexed.register.RegisterItems
import net.backupcup.hexed.util.TagHelper
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.item.Item
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import java.util.concurrent.CompletableFuture

class ItemTagDatagen
    (output: FabricDataOutput?,
                  registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>?
) : FabricTagProvider.ItemTagProvider(output, registriesFuture) {
    override fun configure(arg: RegistryWrapper.WrapperLookup?) {
        builder(Tag) {
            add(RegisterSlagBlocks.BRIMSTONE_SLAG_STAIRS)
            add(RegisterSlagBlocks.BRIMSTONE_BRICKS_STAIRS)
            add(RegisterSlagBlocks.SMOOTH_BRIMSTONE_SLAG_STAIRS)
        }
        builder(BlockTags.SLABS) {
            add(RegisterSlagBlocks.BRIMSTONE_SLAG_SLAB)
            add(RegisterSlagBlocks.BRIMSTONE_BRICKS_SLAB)
            add(RegisterSlagBlocks.SMOOTH_BRIMSTONE_SLAG_SLAB)
        }
        builder(BlockTags.WALLS) {
                add(RegisterSlagBlocks.BRIMSTONE_SLAG_WALL)
                add(RegisterSlagBlocks.BRIMSTONE_BRICKS_WALL)
                add(RegisterSlagBlocks.SMOOTH_BRIMSTONE_SLAG_WALL)
        }
        builder(BlockTags.PICKAXE_MINEABLE) {
            add(RegisterSlagBlocks.LAVENDIN_CINDER)
            add(RegisterSlagBlocks.BRIMSTONE_SLAG)
            add(RegisterSlagBlocks.BRIMSTONE_SLAG_SLAB)
            add(RegisterSlagBlocks.BRIMSTONE_SLAG_STAIRS)
            add(RegisterSlagBlocks.BRIMSTONE_SLAG_WALL)
            add(RegisterSlagBlocks.BRIMSTONE_BRICKS)
            add(RegisterSlagBlocks.BRIMSTONE_BRICKS_SLAB)
            add(RegisterSlagBlocks.BRIMSTONE_BRICKS_STAIRS)
            add(RegisterSlagBlocks.BRIMSTONE_BRICKS_WALL)
            add(RegisterSlagBlocks.SMOOTH_BRIMSTONE_SLAG)
            add(RegisterSlagBlocks.SMOOTH_BRIMSTONE_SLAG_STAIRS)
            add(RegisterSlagBlocks.SMOOTH_BRIMSTONE_SLAG_WALL)
            add(RegisterSlagBlocks.SMOOTH_BRIMSTONE_SLAG_SLAB)
            add(RegisterSlagBlocks.CHISELED_BRIMSTONE_SLAG)
            add(RegisterSlagBlocks.BRIMSTONE_SLAG_PILLAR)
            add(RegisterBlocks.ACCURSED_ALTAR)
        }
    }
    fun builder(tagKey: TagKey<Item>, item: FabricTagProvider<Item>.FabricTagBuilder.() -> Unit) {
        val builder = getOrCreateTagBuilder(tagKey)
        block.invoke(builder)
    }
}

// fun <T> FabricTagProvider<T>.FabricTagBuilder.register(value: T) = this.add(value)  @TODO all this is boilerplate to explain the above 'extension' just to prettify the builder AND later use can make some really complicated things for water to teach me more about at a later date