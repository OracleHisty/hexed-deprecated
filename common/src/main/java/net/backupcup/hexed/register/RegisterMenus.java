package net.backupcup.hexed.register;

import net.backupcup.hexed.altar.AccursedAltarMenu;
import net.backupcup.hexed.registry.Reg;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class RegisterMenus {
    // The typo in "sreen" is kept so the registry id doesn't change.
    public static final RegObject<MenuType<AccursedAltarMenu>> ACCURSED_ALTAR = Reg.register(BuiltInRegistries.MENU,
        "accursed_altar_sreen_handler", () -> new MenuType<>(AccursedAltarMenu::new, FeatureFlags.VANILLA_SET));

    private RegisterMenus() {}

    public static void init() {}
}
