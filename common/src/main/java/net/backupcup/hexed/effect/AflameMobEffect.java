package net.backupcup.hexed.effect;

import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.register.RegisterDamageTypes;
import net.backupcup.hexed.register.RegisterStatusEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Burns with accursed flames: {@code hexed:aflame} damage every second, 1 plus the amplifier. The flames are drawn
 * while the effect lasts (see the {@code LivingEntity} mixin and {@code HexedFlames}), separately from normal fire.
 */
public class AflameMobEffect extends HexMobEffect {
    public AflameMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            entity.hurt(RegisterDamageTypes.of(entity.level(), RegisterDamageTypes.AFLAME_DAMAGE), 1 + amplifier);
        }
        return true;
    }

    /**
     * Sets {@code target} aflame. This gets past effect immunity (bosses, mods that block effects); only the full
     * Calamitous set protects against it.
     */
    public static void apply(LivingEntity target, int duration, int amplifier, @Nullable Entity source) {
        if (HexHelper.hasFullRobes(target)) return;

        MobEffectInstance instance = new MobEffectInstance(RegisterStatusEffects.AFLAME.holder(), duration, amplifier, true, true, true);
        if (!target.addEffect(instance, source) && !target.hasEffect(RegisterStatusEffects.AFLAME.holder())) {
            // The Ender Dragon and the Wither refuse every effect in addEffect itself.
            target.forceAddEffect(instance, source);
        }
    }
}
