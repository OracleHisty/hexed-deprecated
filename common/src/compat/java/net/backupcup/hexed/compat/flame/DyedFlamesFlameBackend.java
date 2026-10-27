package net.backupcup.hexed.compat.flame;

import fuzs.dyedflames.init.ModRegistry;
import net.backupcup.hexed.flame.HexedFlames;
import net.backupcup.hexed.register.RegisterBlocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;

/**
 * Dyed Flames picks an entity's flames from the last fire source block it burned in, using the
 * {@code dyedflames:fire_types} data map ({@code data/dyedflames/data_maps/block/fire_types.json}).
 * Brimstone Fire and Blazing Magma are picked up by Dyed Flames on its own; Aflame uses a technical block as its source.
 */
public class DyedFlamesFlameBackend implements HexedFlames.Backend {
    @Override
    public void register() {}

    private static Block source(HexedFlames.Flame flame) {
        return switch (flame) {
            case AFLAME -> RegisterBlocks.AFLAME_FLAME.get();
            case BRIMSTONE -> RegisterBlocks.BRIMSTONE_FIRE.get();
        };
    }

    @Override
    public void show(Entity entity, HexedFlames.Flame flame) {
        Block source = source(flame);
        if (ModRegistry.LAST_FIRE_SOURCE_ATTACHMENT_TYPE.get(entity) != source) {
            ModRegistry.LAST_FIRE_SOURCE_ATTACHMENT_TYPE.set(entity, source);
        }
    }

    @Override
    public void clear(Entity entity, HexedFlames.Flame flame) {
        if (ModRegistry.LAST_FIRE_SOURCE_ATTACHMENT_TYPE.get(entity) == source(flame)) {
            ModRegistry.LAST_FIRE_SOURCE_ATTACHMENT_TYPE.set(entity, null);
        }
    }
}
