package net.backupcup.hexed.register;

import net.backupcup.hexed.Hexed;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public final class RegisterDamageTypes {
    public static final ResourceKey<DamageType> ABLAZE_DAMAGE = key("ablazed");
    public static final ResourceKey<DamageType> AFLAME_DAMAGE = key("aflame");
    public static final ResourceKey<DamageType> VINDICTIVE_DAMAGE = key("vindictive");
    public static final ResourceKey<DamageType> FRANTIC_DAMAGE = key("frantic");

    //THIS DAMAGE TYPE BYPASSES I-FRAMES
    public static final ResourceKey<DamageType> ABYSSAL_CRUSH = key("abyssal_crush");

    //BASHER EXPLOSION DAMAGE TYPE
    public static final ResourceKey<DamageType> BLAZING_WAVE = key("blazing_wave");

    private RegisterDamageTypes() {}

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, Hexed.id(name));
    }

    public static DamageSource of(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
    }
}
