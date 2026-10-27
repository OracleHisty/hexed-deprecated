package net.backupcup.hexed.neoforge;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.block.BlazingMagmaFluid;
import net.backupcup.hexed.platform.Platform;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class NeoForgePlatform implements Platform {
    private record Pending<T>(RegObject<T> object, Supplier<T> factory) {}

    /** Registrations queued during mod construction, replayed in each registry's {@link RegisterEvent}. */
    private static final Map<ResourceKey<? extends Registry<?>>, List<Pending<?>>> PENDING = new LinkedHashMap<>();

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public <R, T extends R> RegObject<T> register(Registry<R> registry, String name, Supplier<T> factory) {
        RegObject<T> object = new RegObject<>(registry, Hexed.id(name));
        PENDING.computeIfAbsent(registry.key(), key -> new ArrayList<>()).add(new Pending<>(object, factory));
        return object;
    }

    static void onRegister(RegisterEvent event) {
        List<Pending<?>> pending = PENDING.remove(event.getRegistryKey());
        if (pending == null) return;
        for (Pending<?> entry : pending) {
            bind(event, entry);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> void bind(RegisterEvent event, Pending<T> entry) {
        T value = entry.factory().get();
        event.register((ResourceKey) event.getRegistryKey(), entry.object().id(), () -> value);
        entry.object().bind(value);
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder();
    }

    @Override
    public FlowingFluid createBlazingMagmaSource() {
        return new BlazingMagmaFluid.Source() {
            @Override
            public FluidType getFluidType() {
                return HexedNeoForgeFluids.BLAZING_MAGMA_TYPE.get();
            }
        };
    }

    @Override
    public FlowingFluid createBlazingMagmaFlowing() {
        return new BlazingMagmaFluid.Flowing() {
            @Override
            public FluidType getFluidType() {
                return HexedNeoForgeFluids.BLAZING_MAGMA_TYPE.get();
            }
        };
    }
}
