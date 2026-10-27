package net.backupcup.hexed.item;

import net.backupcup.hexed.register.RegisterDamageTypes;
import net.backupcup.hexed.register.RegisterSounds;
import net.backupcup.hexed.register.RegisterStatusEffects;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Disables shields on hit (see the mixins) and, when used, blights the healthiest creature nearby
 * at the cost of the wielder's health.
 */
public class BlightedHarvestItem extends TaintedSwordItem {
    public BlightedHarvestItem(Tier tier, int attackDamage, float attackSpeed, String itemId, Properties properties) {
        super(tier, attackDamage, attackSpeed, itemId, properties);
    }

    /** Ticks until the attack is fully recharged; also used for how long Blighted lasts. */
    public int attackCooldownTicks() {
        return (int) Math.ceil((4f - attackSpeed) * 20);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        if (level.isClientSide()) return super.use(level, user, hand);

        ItemStack stack = user.getItemInHand(hand);
        int attackCooldown = attackCooldownTicks();

        findHealthiestEntity(user).addEffect(new MobEffectInstance(RegisterStatusEffects.BLIGHTED.holder(),
            attackCooldown * 4, 0, false, true, true));

        level.playSound(null, user, RegisterSounds.ACCURSED_ALTAR_TAINT.get(), SoundSource.PLAYERS, 0.5f, 1.5f);
        user.hurt(RegisterDamageTypes.of(level, RegisterDamageTypes.ABLAZE_DAMAGE), user.getHealth() / 10f);
        user.getCooldowns().addCooldown(this, attackCooldown * 2);
        stack.hurtAndBreak(5, user, LivingEntity.getSlotForHand(hand));
        return super.use(level, user, hand);
    }

    /** The creature in range with the highest max health to current health ratio. */
    private static LivingEntity findHealthiestEntity(LivingEntity user) {
        AABB area = new AABB(user.blockPosition()).inflate(16.0 + 2 * (user.getMaxHealth() - user.getHealth()));
        LivingEntity best = user;
        float bestRatio = 0f;
        for (LivingEntity entity : user.level().getEntitiesOfClass(LivingEntity.class, area)) {
            float ratio = entity.getMaxHealth() / entity.getHealth();
            if (ratio > bestRatio) {
                bestRatio = ratio;
                best = entity;
            }
        }
        return best;
    }
}
