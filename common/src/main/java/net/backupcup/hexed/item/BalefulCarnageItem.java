package net.backupcup.hexed.item;

import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.register.RegisterStatusEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

/** Stacks Death Mark on targets, which turns into Vindictive at four stacks. Each hit also weighs on the wielder. */
public class BalefulCarnageItem extends TaintedSwordItem {
    public BalefulCarnageItem(Tier tier, int attackDamage, float attackSpeed, String itemId, Properties properties) {
        super(tier, attackDamage, attackSpeed, itemId, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide()) return super.hurtEnemy(stack, target, attacker);

        HexHelper.multiplyingEffect(target, RegisterStatusEffects.DEATH_MARK.holder(), 200, 50, false, true, true);

        MobEffectInstance ironclad = attacker.getEffect(RegisterStatusEffects.IRONCLAD.holder());
        if (ironclad == null || ironclad.getAmplifier() < attacker.getMaxHealth() / 2f) {
            HexHelper.multiplyingEffect(attacker, RegisterStatusEffects.IRONCLAD.holder(), 80, 5);
        }

        MobEffectInstance deathMark = target.getEffect(RegisterStatusEffects.DEATH_MARK.holder());
        if ((deathMark != null && deathMark.getAmplifier() >= 4) || target.hasEffect(RegisterStatusEffects.VINDICTIVE.holder())) {
            target.removeEffect(RegisterStatusEffects.DEATH_MARK.holder());
            HexHelper.multiplyingEffect(target, RegisterStatusEffects.VINDICTIVE.holder(), 100, 20, false, true, true);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
