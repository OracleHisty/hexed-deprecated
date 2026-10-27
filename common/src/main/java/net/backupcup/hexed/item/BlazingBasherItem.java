package net.backupcup.hexed.item;

import net.backupcup.hexed.register.RegisterStatusEffects;
import net.backupcup.hexed.util.TimedEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

/** Kills set off a delayed blazing wave; near-full-health targets are set ablaze, at the cost of the wielder. */
public class BlazingBasherItem extends TaintedSwordItem {
    public BlazingBasherItem(Tier tier, int attackDamage, float attackSpeed, String itemId, Properties properties) {
        super(tier, attackDamage, attackSpeed, itemId, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide()) return super.hurtEnemy(stack, target, attacker);

        if (target.isDeadOrDying()) {
            TimedEvents.schedule(20, () -> BlazingWaveHelper.createExplosion(target, 5.0));
        } else if (target.getHealth() + attackDamage >= target.getMaxHealth()) {
            target.addEffect(new MobEffectInstance(RegisterStatusEffects.ABLAZE.holder(), 60, 0, false, true, true));
            attacker.addEffect(new MobEffectInstance(RegisterStatusEffects.AFLAME.holder(), 30, 0, false, true, true));
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
