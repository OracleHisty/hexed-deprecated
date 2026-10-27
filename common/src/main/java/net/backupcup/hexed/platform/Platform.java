package net.backupcup.hexed.platform;

import net.backupcup.hexed.registry.RegObject;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.FlowingFluid;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Everything the common code needs from the mod loader. Implemented once per loader and looked up through
 * {@link Services#PLATFORM}.
 */
public interface Platform {
    String getPlatformName();

    boolean isModLoaded(String modId);

    Path getConfigDir();

    /**
     * Queues (NeoForge) or immediately performs (Fabric) a registration. The returned object resolves once the
     * loader has actually registered the value.
     */
    <R, T extends R> RegObject<T> register(Registry<R> registry, String name, Supplier<T> factory);

    CreativeModeTab.Builder creativeTabBuilder();

    /** NeoForge fluids need a {@code FluidType}, so the loader supplies the concrete fluid instances. */
    FlowingFluid createBlazingMagmaSource();

    FlowingFluid createBlazingMagmaFlowing();
}
