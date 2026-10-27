package net.backupcup.hexed.register;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.effect.*;
import net.backupcup.hexed.registry.Reg;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Supplier;

public final class RegisterStatusEffects {
    //Aflame Hex
    public static final RegObject<MobEffect> AFLAME = effect("aflame", () -> new AflameMobEffect(MobEffectCategory.BENEFICIAL, 0x7a3045));
    public static final RegObject<MobEffect> ABLAZE = effect("ablaze", () -> new AblazeMobEffect(MobEffectCategory.BENEFICIAL, 0xae2334));

    //Persecuted Hex
    public static final RegObject<MobEffect> ETHEREAL = effect("ethereal", () -> new HexMobEffect(MobEffectCategory.BENEFICIAL, 0xe83b3b)
        .addAttributeModifier(Attributes.ARMOR, Hexed.id("effect.ethereal"), -1.0, AttributeModifier.Operation.ADD_VALUE));

    //Ephemeral Hex
    public static final RegObject<MobEffect> EXHAUSTION = effect("exhaustion", () -> new HexMobEffect(MobEffectCategory.BENEFICIAL, 0xe6904e));

    //Vindictive Hex
    public static final RegObject<MobEffect> VINDICTIVE = effect("vindictive", () -> new VindictiveMobEffect(MobEffectCategory.BENEFICIAL, 0xf4a58d));
    public static final RegObject<MobEffect> SMOULDERING = effect("smouldering", () -> new HexMobEffect(MobEffectCategory.BENEFICIAL, 0xcddf6c));

    //Traitorous Hex
    public static final RegObject<MobEffect> TRAITOROUS = effect("traitorous", () -> new HexMobEffect(MobEffectCategory.BENEFICIAL, 0xe83b3b));

    //Ironclad Hex
    public static final RegObject<MobEffect> IRONCLAD = effect("ironclad", () -> new HexMobEffect(MobEffectCategory.BENEFICIAL, 0xfb6b1d)
        .addAttributeModifier(Attributes.MAX_HEALTH, Hexed.id("effect.ironclad"), -2.0, AttributeModifier.Operation.ADD_VALUE));

    //Frantic Hex
    public static final RegObject<MobEffect> FRANTIC = effect("frantic", () -> new HexMobEffect(MobEffectCategory.BENEFICIAL, 0x7a3045)
        .addAttributeModifier(Attributes.MOVEMENT_SPEED, Hexed.id("effect.frantic"), 0.025, AttributeModifier.Operation.ADD_VALUE));

    //Overburden Hex
    public static final RegObject<MobEffect> OVERBURDEN = effect("overburden", () -> new OverburdenMobEffect(MobEffectCategory.BENEFICIAL, 0xe83b3b)
        .addAttributeModifier(Attributes.MOVEMENT_SPEED, Hexed.id("effect.overburden"), -0.00390625, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

    //HARVEST OF THE BLIGHTED DEBUFF
    public static final RegObject<MobEffect> BLIGHTED = effect("blighted", () -> new HexMobEffect(MobEffectCategory.BENEFICIAL, 0xe6904e));

    //BALEFUL CARNAGE DEBUFF
    public static final RegObject<MobEffect> DEATH_MARK = effect("death_mark", () -> new HexMobEffect(MobEffectCategory.BENEFICIAL, 0x7a3045));

    private RegisterStatusEffects() {}

    private static RegObject<MobEffect> effect(String name, Supplier<MobEffect> factory) {
        return Reg.register(BuiltInRegistries.MOB_EFFECT, name, factory);
    }

    public static void init() {}
}
