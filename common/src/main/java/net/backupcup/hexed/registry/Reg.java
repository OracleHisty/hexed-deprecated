package net.backupcup.hexed.registry;

import net.backupcup.hexed.platform.Services;
import net.minecraft.core.Registry;

import java.util.function.Supplier;

public final class Reg {
    private Reg() {}

    public static <R, T extends R> RegObject<T> register(Registry<R> registry, String name, Supplier<T> factory) {
        return Services.PLATFORM.register(registry, name, factory);
    }
}
