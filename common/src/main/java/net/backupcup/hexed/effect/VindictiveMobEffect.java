package net.backupcup.hexed.effect;

import net.backupcup.hexed.register.RegisterDamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** When the top level runs out, the target takes damage equal to the level and the whole stack is cleared. */
public class VindictiveMobEffect extends HexMobEffect {
    public VindictiveMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration == 1;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return true;
        entity.hurt(RegisterDamageTypes.of(entity.level(), RegisterDamageTypes.VINDICTIVE_DAMAGE), amplifier + 1);
        // Returning false removes the effect along with the lower levels waiting behind it.
        return false;
    }
}
