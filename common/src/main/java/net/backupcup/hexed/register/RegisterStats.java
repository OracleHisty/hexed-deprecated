package net.backupcup.hexed.register;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.registry.Reg;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class RegisterStats {
    // The typo in "accused" is kept so existing stat data still matches.
    public static final RegObject<ResourceLocation> ACCURSED_ALTAR_OPENED = stat("accused_altar_opened");
    public static final RegObject<ResourceLocation> ITEMS_HEXED = stat("items_hexed");

    private RegisterStats() {}

    private static RegObject<ResourceLocation> stat(String name) {
        return Reg.register(BuiltInRegistries.CUSTOM_STAT, name, () -> Hexed.id(name));
    }

    public static void init() {}
}
