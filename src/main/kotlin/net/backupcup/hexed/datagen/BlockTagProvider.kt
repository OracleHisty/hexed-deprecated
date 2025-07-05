package net.backupcup.hexed.datagen

import net.backupcup.hexed.register.RegisterBlocks
import net.backupcup.hexed.register.RegisterSlagBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.block.Block
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey
import java.util.concurrent.CompletableFuture

class BlockTagProvider
    (output: FabricDataOutput?,
                  registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>?
) : FabricTagProvider.BlockTagProvider(output, registriesFuture) {
    override fun configure(arg: RegistryWrapper.WrapperLookup?) {
        builder(BlockTags.STAIRS) {
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
    fun builder(tagKey: TagKey<Block>, block: FabricTagProvider<Block>.FabricTagBuilder.() -> Unit) {
        val builder = getOrCreateTagBuilder(tagKey)
        block.invoke(builder)
    }
}

// fun <T> FabricTagProvider<T>.FabricTagBuilder.register(value: T) = this.add(value)  @TODO all this is boilerplate to explain the above 'extension' just to prettify the builder AND later use can make some really complicated things for water to teach me more about at a later date