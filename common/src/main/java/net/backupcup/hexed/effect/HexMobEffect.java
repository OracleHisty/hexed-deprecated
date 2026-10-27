package net.backupcup.hexed.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** A plain effect whose logic lives elsewhere (mixins, hex effects). */
public class HexMobEffect extends MobEffect {
    public HexMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
