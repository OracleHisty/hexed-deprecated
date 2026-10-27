package net.backupcup.hexed.hex;

import net.backupcup.hexed.Config;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.network.BloodthirstyParticlesPayload;
import net.backupcup.hexed.network.HexedNetwork;
import net.backupcup.hexed.register.RegisterStatusEffects;
import net.backupcup.hexed.util.HexRandom;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The on-hit behavior of the hexes that used to override {@code Enchantment#onTargetDamaged} and
 * {@code Enchantment#onUserDamaged}. Like those hooks, every equipped item is checked, not only the weapon.
 */
public final class HexEffects {
    private HexEffects() {}

    /** Called after an attack lands, from {@code EnchantmentHelper#doPostAttackEffectsWithItemSource}. */
    public static void onPostAttack(ServerLevel level, Entity target, DamageSource source) {
        if (target instanceof LivingEntity victim) {
            for (ResourceKey<Enchantment> hex : equippedHexes(victim)) {
                onUserDamaged(hex, victim, source.getEntity());
            }
        }
        if (source.getEntity() instanceof LivingEntity attacker) {
            for (ResourceKey<Enchantment> hex : equippedHexes(attacker)) {
                onTargetDamaged(hex, level, attacker, target);
            }
        }
    }

    private static List<ResourceKey<Enchantment>> equippedHexes(LivingEntity entity) {
        List<ResourceKey<Enchantment>> hexes = new ArrayList<>();
        for (ItemStack stack : HexHelper.allEquipment(entity)) {
            for (Holder<Enchantment> enchantment : HexHelper.enchantments(stack).keySet()) {
                enchantment.unwrapKey().filter(Hexes::isHex).ifPresent(hexes::add);
            }
        }
        return hexes;
    }

    private static void onTargetDamaged(ResourceKey<Enchantment> hex, ServerLevel level, LivingEntity user, Entity target) {
        if (hex.equals(Hexes.AFLAME)) aflame(user, target);
        else if (hex.equals(Hexes.EPHEMERAL)) ephemeral(user, target);
        else if (hex.equals(Hexes.TRAITOROUS)) traitorous(user, target);
        else if (hex.equals(Hexes.VINDICTIVE)) vindictive(user, target);
        else if (hex.equals(Hexes.BLOODTHIRSTY)) bloodthirsty(user, target);
    }

    private static void onUserDamaged(ResourceKey<Enchantment> hex, LivingEntity user, @Nullable Entity attacker) {
        if (hex.equals(Hexes.PERSECUTED)) persecuted(user);
        else if (hex.equals(Hexes.DISFIGUREMENT)) disfigurement(user);
    }

    private static void aflame(LivingEntity user, Entity target) {
        if (!(target instanceof LivingEntity living)) return;
        Config.AflameHexParams config = Hexed.config().aflameHex;

        if (living.isAlive()) {
            living.addEffect(new MobEffectInstance(RegisterStatusEffects.ABLAZE.holder(), config.AblazeDuration, config.AblazeAmplifier, true, true, true));
        }
        if (user.isAlive() && !HexHelper.hasFullRobes(user)) {
            user.addEffect(new MobEffectInstance(RegisterStatusEffects.AFLAME.holder(), config.AflameDuration, config.AflameAmplifier, true, true, true));
        }
    }

    private static void ephemeral(LivingEntity user, Entity target) {
        if (!(target instanceof LivingEntity) || HexHelper.hasFullRobes(user)) return;
        Config.EphemeralHexParams config = Hexed.config().ephemeralHex;
        HexHelper.multiplyingEffect(user, RegisterStatusEffects.EXHAUSTION.holder(), config.debuffDuration, config.debuffDecayLength);
    }

    /** Backstabs (attacker and target facing roughly the same way) inflict Traitorous. */
    private static void traitorous(LivingEntity user, Entity target) {
        float targetYaw = target.getYRot() % 360;
        if (targetYaw > 180) targetYaw -= 360;
        float userYaw = user.getYRot() % 360;
        if (userYaw > 180) userYaw -= 360;

        if (Math.abs(targetYaw - userYaw) > 67.5 || !(target instanceof LivingEntity living) || !(user instanceof Player player)) return;
        Config.TraitorousHexParams config = Hexed.config().traitorousHex;

        living.addEffect(new MobEffectInstance(RegisterStatusEffects.TRAITOROUS.holder(), config.debuffDuration, 0, true, true, true));
        if (!HexHelper.hasFullRobes(user) && HexHelper.has(user.getMainHandItem(), Hexes.TRAITOROUS)) {
            player.getCooldowns().addCooldown(user.getMainHandItem().getItem(), config.cooldownDuration);
        }
    }

    private static void vindictive(LivingEntity user, Entity target) {
        Config.VindictiveHexParams config = Hexed.config().vindictiveHex;
        if (target instanceof LivingEntity living) {
            MobEffectInstance current = living.getEffect(RegisterStatusEffects.VINDICTIVE.holder());
            if (current == null || current.getAmplifier() < config.maxAmplifier) {
                HexHelper.multiplyingEffect(living, RegisterStatusEffects.VINDICTIVE.holder(), config.vindictiveDuration, config.vindictiveDecayLength);
            }
        }
        if (!HexHelper.hasFullRobes(user)) {
            HexHelper.multiplyingEffect(user, RegisterStatusEffects.SMOULDERING.holder(), config.smoulderingDuration, config.smoulderingDecayLength);
        }
    }

    /** Killing blows heal the wearer based on the victim's max health. Overflow feeds Metamorphosis. */
    private static void bloodthirsty(LivingEntity user, Entity target) {
        if (!(target instanceof LivingEntity living) || !living.isDeadOrDying()) return;
        Config config = Hexed.config();
        Config.BloodthirstyHexParams params = config.bloodthirstyHex;

        float scaled = living.getMaxHealth() * params.healModifier;
        float heal = scaled >= params.minimumHealAmount ? Math.min(params.maximumHealAmount, scaled) : params.minimumHealAmount;

        int particles = HexRandom.nextInt(10, living.getMaxHealth() > 20 ? (int) Math.min(living.getMaxHealth(), 40f) : 20);
        HexedNetwork.sendToTracking(living, new BloodthirstyParticlesPayload(particles, living.position()));

        if (user.getMaxHealth() < user.getHealth() + heal
            && HexHelper.has(user.getItemBySlot(EquipmentSlot.HEAD), Hexes.METAMORPHOSIS)
            && user instanceof Player player) {
            float overflow = heal - (user.getMaxHealth() - user.getHealth());
            player.getFoodData().eat((int) (overflow * config.metamorphosisHex.foodModifier), config.metamorphosisHex.saturationAmount);
        }
        user.setHealth(user.getHealth() + heal);
    }

    /** Getting hit strips armor through stacked Ethereal, one level per armor point. */
    private static void persecuted(LivingEntity user) {
        if (user.hasEffect(RegisterStatusEffects.ETHEREAL.holder())) return;

        int armor = user.getArmorValue();
        int durationModifier = Hexed.config().persecutedHex.debuffDurationModifier;
        for (int i = 0; i <= armor; i++) {
            user.addEffect(new MobEffectInstance(RegisterStatusEffects.ETHEREAL.holder(), 1 + (armor - i) * durationModifier, i, true, false, true));
        }
    }

    private static void disfigurement(LivingEntity user) {
        Config.DisfigurementHexParams config = Hexed.config().disfigurementHex;
        int radius = config.boxRadius;
        AABB area = new AABB(user.getX() - radius, user.getY() - radius, user.getZ() - radius,
            user.getX() + radius, user.getY() + radius, user.getZ() + radius);

        boolean robes = HexHelper.hasFullRobes(user);
        for (LivingEntity entity : user.level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (robes && entity == user) continue;
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, config.weaknessDuration, config.weaknessAmplifier, true, true, true));
            entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, config.hungerDuration, config.hungerAmplifier, true, true, true));
        }
    }
}
