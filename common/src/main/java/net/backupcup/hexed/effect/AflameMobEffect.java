package net.backupcup.hexed.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Sets the wearer on fire, more often at higher levels. */
public class AflameMobEffect extends HexMobEffect {
    public AflameMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = 40 >> amplifier;
        return interval <= 0 || duration % interval == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.igniteForSeconds(2 + (1 << amplifier));
        return true;
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        entity.igniteForSeconds(2 + (1 << amplifier));
    }
}
