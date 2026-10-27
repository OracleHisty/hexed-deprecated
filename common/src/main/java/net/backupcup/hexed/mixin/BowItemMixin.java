package net.backupcup.hexed.mixin;

import net.backupcup.hexed.Config;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.backupcup.hexed.network.HexUiUpdatePayload;
import net.backupcup.hexed.network.HexedNetwork;
import net.backupcup.hexed.register.RegisterSounds;
import net.backupcup.hexed.util.AggravateData;
import net.backupcup.hexed.util.HexPlayerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(BowItem.class)
public abstract class BowItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void hexed$aggravatePassHand(Level level, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (user instanceof ServerPlayer && HexHelper.has(user.getItemInHand(hand), Hexes.AGGRAVATE)) {
            ((HexPlayerData) user).hexed$setAggravateData(new AggravateData(hand, 0, 0));
        }
    }

    /** Releases the stored Aggravate charges as a volley. Seven charges (an overcharge) also explode. */
    @Inject(method = "releaseUsing", at = @At("TAIL"))
    private void hexed$aggravateShoot(ItemStack stack, Level level, LivingEntity user, int timeLeft, CallbackInfo ci) {
        if (!(user instanceof ServerPlayer player) || !HexHelper.has(stack, Hexes.AGGRAVATE)) return;

        Config.AggravateHexParams config = Hexed.config().aggravateHex;
        HexPlayerData data = (HexPlayerData) player;
        int charge = data.hexed$getAggravateData().chargeAmount;

        if (charge > 0) {
            if (charge == 7 && !HexHelper.hasFullRobes(player)) {
                level.explode(null, player.getX(), player.getBoundingBox().getCenter().y, player.getZ(),
                    config.explosionPower, Level.ExplosionInteraction.NONE);
            }

            ArrowItem arrowItem = (ArrowItem) Items.ARROW;
            for (int i = 0; i < charge * config.arrowsPerCharge; i++) {
                AbstractArrow arrow = arrowItem.createArrow(level, new ItemStack(Items.ARROW), player, stack);
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 3.0f, 20.0f);
                arrow.setCritArrow(true);
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
                level.addFreshEntity(arrow);
                if (stack.isEmpty()) break;
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), RegisterSounds.ACCURSED_ALTAR_TAINT.get(),
                SoundSource.PLAYERS, 0.25f - (1f / charge) / 8, 2f);
        }

        data.hexed$setAggravateData(new AggravateData(InteractionHand.MAIN_HAND, 0, 0));
        HexedNetwork.sendToPlayer(player, HexUiUpdatePayload.of(HexUiUpdatePayload.Ui.AGGRAVATE, 0));
    }

    /** Every fifth fully drawn shot is replaced by a piercing hitscan beam. */
    @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
    private void hexed$phasedLogic(ItemStack stack, Level level, LivingEntity user, int timeLeft, CallbackInfo ci) {
        if (!(user instanceof ServerPlayer player) || !HexHelper.has(stack, Hexes.PHASED)) return;

        HexPlayerData data = (HexPlayerData) player;
        if (data.hexed$getPullPredicate() < 1) return;

        int charge = data.hexed$getPhasedCharge();
        if (charge >= 4) {
            charge = 0;
            player.playNotifySound(RegisterSounds.PHASED_SHOT.get(), SoundSource.PLAYERS, 0.5f, 1f);
            if (!HexHelper.hasFullRobes(player)) {
                player.getCooldowns().addCooldown(stack.getItem(), 200);
            }
            hexed$hitscan(stack, level, player);
            ci.cancel();
        } else {
            charge++;
            player.playNotifySound(RegisterSounds.PHASED_TIER.get(), SoundSource.PLAYERS, 0.5f, 1f + charge / 4f);
        }

        data.hexed$setPhasedCharge(charge);
        HexedNetwork.sendToPlayer(player, HexUiUpdatePayload.of(HexUiUpdatePayload.Ui.PHASED, charge));
    }

    @Unique
    private static void hexed$hitscan(ItemStack stack, Level level, ServerPlayer player) {
        Config.PhasedHexParams config = Hexed.config().phasedHex;
        double distance = config.hitscanDistance;
        double stepSize = 0.5;
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 direction = player.getViewVector(1.0f);
        float initialDamage = config.initialDamage + config.powerModifier * HexHelper.level(stack, Enchantments.POWER);
        boolean flame = HexHelper.level(stack, Enchantments.FLAME) > 0;

        List<LivingEntity> hits = new ArrayList<>();
        for (int i = 0; i < (int) (distance / stepSize); i++) {
            Vec3 point = start.add(direction.scale(i * stepSize));
            BlockPos pos = BlockPos.containing(point);
            if (!level.getBlockState(pos).isAir() || point.y <= level.getMinBuildHeight()) break;

            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(1))) {
                if (entity == player) continue;
                if (!hits.contains(entity)) hits.add(entity);

                entity.hurt(level.damageSources().magic(), initialDamage * (1f + config.damageMultiplier * hits.size()));
                if (flame) entity.igniteForSeconds(config.flameSeconds);
            }

            player.serverLevel().sendParticles(hits.isEmpty() ? ParticleTypes.GLOW : ParticleTypes.SONIC_BOOM,
                point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0);
        }
    }
}
