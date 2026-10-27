package net.backupcup.hexed.register;

import com.mojang.serialization.Codec;
import net.backupcup.hexed.registry.Reg;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;

/** Item stack data that used to live in NBT. */
public final class RegisterDataComponents {
    /** Overclock hex: crossbow overheat stacks, which shorten the charge time. */
    public static final RegObject<DataComponentType<Integer>> OVERHEAT = Reg.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "overheat",
        () -> DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    /** Bishop's Labrys: stored electric charges. */
    public static final RegObject<DataComponentType<Integer>> ELECTRIC_CHARGE = Reg.register(BuiltInRegistries.DATA_COMPONENT_TYPE, "electric_charge",
        () -> DataComponentType.<Integer>builder().persistent(Codec.intRange(0, 2)).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    private RegisterDataComponents() {}

    public static void init() {}
}
