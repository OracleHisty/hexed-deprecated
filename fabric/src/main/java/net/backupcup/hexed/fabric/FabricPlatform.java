package net.backupcup.hexed.fabric;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.block.BlazingMagmaFluid;
import net.backupcup.hexed.platform.Platform;
import net.backupcup.hexed.registry.RegObject;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.FlowingFluid;

import java.nio.file.Path;
import java.util.function.Supplier;

public class FabricPlatform implements Platform {
    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public <R, T extends R> RegObject<T> register(Registry<R> registry, String name, Supplier<T> factory) {
        RegObject<T> object = new RegObject<>(registry, Hexed.id(name));
        object.bind(Registry.register(registry, object.id(), factory.get()));
        return object;
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return FabricItemGroup.builder();
    }

    @Override
    public FlowingFluid createBlazingMagmaSource() {
        return new BlazingMagmaFluid.Source();
    }

    @Override
    public FlowingFluid createBlazingMagmaFlowing() {
        return new BlazingMagmaFluid.Flowing();
    }
}
