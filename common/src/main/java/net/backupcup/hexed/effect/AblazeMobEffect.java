package net.backupcup.hexed.effect;

import net.backupcup.hexed.register.RegisterDamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Burns through armor, more often at higher levels. */
public class AblazeMobEffect extends HexMobEffect {
    public AblazeMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = 40 >> amplifier;
        return interval <= 0 || duration % interval == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        burn(entity);
        return true;
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        burn(entity);
    }

    private static void burn(LivingEntity entity) {
        if (entity.level().isClientSide()) return;
        entity.hurt(RegisterDamageTypes.of(entity.level(), RegisterDamageTypes.ABLAZE_DAMAGE), 1f);
    }
}
