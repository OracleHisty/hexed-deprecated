package net.backupcup.hexed.item;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.register.RegisterDamageTypes;
import net.backupcup.hexed.register.RegisterDataComponents;
import net.backupcup.hexed.register.RegisterSounds;
import net.backupcup.hexed.util.CustomHandTexture;
import net.backupcup.hexed.util.TaintedItem;
import net.backupcup.hexed.util.TimedEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Using it stores up to two electric charges; a charged hit sends a spark chaining between nearby creatures. */
public class BishopLabrysItem extends AxeItem implements CustomHandTexture, TaintedItem {
    private final String itemId;

    public BishopLabrysItem(Tier tier, float attackDamage, float attackSpeed, String itemId, Properties properties) {
        super(tier, properties.attributes(AxeItem.createAttributes(tier, attackDamage, attackSpeed)));
        this.itemId = itemId;
    }

    public static int getCharge(ItemStack stack) {
        return stack.getOrDefault(RegisterDataComponents.ELECTRIC_CHARGE.get(), 0);
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public ResourceLocation getHandModel() {
        return Hexed.id("item/" + itemId + "_hand");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hexed." + itemId).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        if (level.isClientSide()) return super.use(level, user, hand);

        ItemStack stack = user.getItemInHand(hand);
        int charge = getCharge(stack);
        if (charge < 2) {
            charge++;
            level.playSound(null, user, RegisterSounds.ELECTRIC_CHARGE.get(), SoundSource.PLAYERS, 1f, 1f + 0.25f * charge);
            stack.set(RegisterDataComponents.ELECTRIC_CHARGE.get(), charge);
            stack.hurtAndBreak(5 * charge, user, LivingEntity.getSlotForHand(hand));
            user.getCooldowns().addCooldown(this, 100);
        }
        return super.use(level, user, hand);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!(attacker.level() instanceof ServerLevel level)) return super.hurtEnemy(stack, target, attacker);

        int charge = getCharge(stack);
        if (charge > 0) {
            charge--;
            List<LivingEntity> chain = findNearestEntities(target, charge);
            chain.remove(target);

            // Only the enchantment bonus carries over to the spark, as before.
            float damage = EnchantmentHelper.modifyDamage(level, stack, target, level.damageSources().mobAttack(attacker), 0f);
            createRecursiveSpark(damage, level, chain, target);

            level.playSound(null, target, RegisterSounds.ELECTRIC_DISCHARGE.get(), SoundSource.PLAYERS, 0.25f, 1f);
            if (attacker instanceof ServerPlayer player) {
                player.getCooldowns().addCooldown(this, 20);
            }
            stack.set(RegisterDataComponents.ELECTRIC_CHARGE.get(), charge);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private static List<LivingEntity> findNearestEntities(LivingEntity target, int charge) {
        List<LivingEntity> chain = new ArrayList<>();
        chain.add(target);
        for (int i = 1; i <= 3 * (charge + 1); i++) {
            LivingEntity current = chain.get(chain.size() - 1);
            AABB area = new AABB(current.blockPosition()).inflate(10.0 + 5.0 * charge);
            LivingEntity closest = null;
            float closestDistance = 0f;
            for (LivingEntity found : current.level().getEntitiesOfClass(LivingEntity.class, area)) {
                if (chain.contains(found)) continue;
                float distance = target.distanceTo(found);
                if (closest == null || distance < closestDistance) {
                    closest = found;
                    closestDistance = distance;
                }
            }
            if (closest == null) break;
            chain.add(closest);
        }
        return chain;
    }

    /** Jumps to the next creature in the chain every few ticks, losing a fifth of its damage each time. */
    private static void createRecursiveSpark(float damage, ServerLevel level, List<LivingEntity> chain, LivingEntity previous) {
        if (chain.isEmpty()) return;
        TimedEvents.schedule(4, () -> {
            LivingEntity current = chain.get(0);
            previous.hurt(RegisterDamageTypes.of(level, DamageTypes.LIGHTNING_BOLT), damage);
            particleBeam(level,
                previous.position().add(0.0, previous.getBbHeight() / 2.0, 0.0),
                current.position().add(0.0, current.getBbHeight() / 2.0, 0.0));
            createRecursiveSpark(damage * 0.8f, level, chain.subList(1, chain.size()), current);
        });
    }

    private static void particleBeam(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 step = end.subtract(start).scale(0.1);
        Vec3 pos = start;
        for (int i = 1; i <= 10; i++) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 5, step.x, step.y, step.z, 0.0);
            pos = pos.add(step);
        }
    }
}
