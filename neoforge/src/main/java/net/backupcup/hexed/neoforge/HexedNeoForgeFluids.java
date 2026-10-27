package net.backupcup.hexed.neoforge;

import net.backupcup.hexed.registry.Reg;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** NeoForge requires every fluid to have a {@link FluidType}; this one copies lava's properties. */
public final class HexedNeoForgeFluids {
    public static final RegObject<FluidType> BLAZING_MAGMA_TYPE = Reg.register(NeoForgeRegistries.FLUID_TYPES, "blazing_magma",
        () -> new FluidType(FluidType.Properties.create()
            .descriptionId("block.hexed.blazing_magma")
            .canSwim(false)
            .canDrown(false)
            .pathType(PathType.LAVA)
            .adjacentPathType(null)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
            .lightLevel(15)
            .density(3000)
            .viscosity(6000)
            .temperature(1300)));

    private HexedNeoForgeFluids() {}

    public static void init() {}
}
