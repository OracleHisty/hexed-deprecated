package net.backupcup.hexed.mixin;

import net.backupcup.hexed.Config;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.entity.BlazingSkullEntity;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.backupcup.hexed.network.HexedNetwork;
import net.backupcup.hexed.network.LingerParticlePayload;
import net.backupcup.hexed.register.RegisterEntities;
import net.backupcup.hexed.register.RegisterSounds;
import net.backupcup.hexed.register.RegisterStatusEffects;
import net.backupcup.hexed.util.HexRandom;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow {
    @Shadow @Final private static EntityDataAccessor<Byte> ID_LOYALTY;

    @Unique @Nullable private Entity hexed$seizedEntity = null;

    protected ThrownTridentMixin(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    @Unique
    private ItemStack hexed$trident() {
        return getPickupItemStackOrigin();
    }

    /** A grounded Linger trident burns everything around it, wearing itself down for each creature. */
    @Inject(method = "tick", at = @At("HEAD"))
    private void hexed$lingerAura(CallbackInfo ci) {
        if (!inGround || !(level() instanceof ServerLevel level) || !HexHelper.has(hexed$trident(), Hexes.LINGER)) return;

        Config.LingerHexParams config = Hexed.config().lingerHex;
        int radius = config.lingerRadius;

        if (level.getGameTime() % 20 == 0) {
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius))) {
                entity.addEffect(new MobEffectInstance(RegisterStatusEffects.ABLAZE.holder(), config.debuffDuration, config.debuffAmplifier, true, false, true));

                hexed$trident().hurtAndBreak(config.tridentDamage, level, null, item -> {
                    level.playSound(null, blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS);
                    discard();
                });
                if (isRemoved()) return;
            }
        }

        Vec3 pos = new Vec3(
            getX() + (random.nextDouble() * 2 - 1) * radius,
            getY() + random.nextDouble() * radius,
            getZ() + (random.nextDouble() * 2 - 1) * radius);
        HexedNetwork.sendToTracking(this, new LingerParticlePayload(pos));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void hexed$flaringSummonFire(CallbackInfo ci) {
        if (inGround || level().isClientSide() || !HexHelper.has(hexed$trident(), Hexes.FLARING)) return;

        if (level().getEntitiesOfClass(FallingBlockEntity.class, new AABB(blockPosition()).inflate(1)).isEmpty()) {
            FallingBlockEntity.fall(level(), blockPosition(),
                Hexed.config().flaringHex.isSoulFire ? Blocks.SOUL_FIRE.defaultBlockState() : Blocks.FIRE.defaultBlockState());
        }
    }

    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void hexed$seizeGetEntity(EntityHitResult hit, CallbackInfo ci) {
        if (HexHelper.has(hexed$trident(), Hexes.SEIZE)) {
            hexed$seizedEntity = hit.getEntity();
        }
    }

    /** Seize drags the hit creature along with a Loyalty trident, or yanks it towards the thrower otherwise. */
    @Inject(method = "tick", at = @At("HEAD"))
    private void hexed$seizePull(CallbackInfo ci) {
        if (!(hexed$seizedEntity instanceof LivingEntity seized) || level().isClientSide()) return;

        Config.SeizeHexParams config = Hexed.config().seizeHex;
        Entity owner = getOwner();
        int maxPullableHP = config.maxPullableHP;
        if (owner instanceof LivingEntity livingOwner && HexHelper.hasFullRobes(livingOwner)) maxPullableHP *= 2;

        if (seized.getMaxHealth() > maxPullableHP || seized.isBlocking()) return;

        if (entityData.get(ID_LOYALTY) > 0) {
            seized.setPos(getX(), getY() - seized.getBbHeight() / 2, getZ());
        } else if (owner != null) {
            Vec3 direction = owner.position().subtract(seized.position()).normalize();
            double distance = owner.position().distanceTo(seized.position());
            double maxDistance = distance / 2;
            double strength = config.minimumPullStrength
                + (config.maximumPullStrength - config.minimumPullStrength) * (1 - Math.min(distance / maxDistance, 1));

            seized.push(direction.scale(strength));
            seized.hurtMarked = true;
            hexed$seizedEntity = null;
        } else {
            hexed$seizedEntity = null;
        }
    }

    /** Sepulture summons three Blazing Skulls on hit. Some of them may turn on the thrower. */
    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void hexed$sepultureSkulls(EntityHitResult hit, CallbackInfo ci) {
        if (level().isClientSide() || !HexHelper.has(hexed$trident(), Hexes.SEPULTURE) || !(hit.getEntity() instanceof LivingEntity)) return;
        if (hit.getEntity().getType() == RegisterEntities.BLAZING_SKULL.get()) return;

        Config.SepultureHexParams config = Hexed.config().sepultureHex;
        Entity owner = getOwner();
        boolean robes = owner instanceof LivingEntity livingOwner && HexHelper.hasFullRobes(livingOwner);

        for (int i = 0; i < 3; i++) {
            double angle = i * 2 * Math.PI / 3;
            Vec3 spawnPos = new Vec3(getX() + Math.cos(angle), getY() + 0.5, getZ() + Math.sin(angle));
            Vec3 movement = new Vec3((spawnPos.x - getX()) / 4, 0.333, (spawnPos.z - getZ()) / 4);

            LivingEntity target = !robes && HexRandom.nextDouble() <= config.angerChance && owner instanceof LivingEntity livingOwner
                ? livingOwner : null;
            level().addFreshEntity(new BlazingSkullEntity(level(), spawnPos, movement, target, config.explosionPower));
        }

        level().playSound(null, blockPosition(), RegisterSounds.ACCURSED_ALTAR_HEX.get(), SoundSource.HOSTILE,
            HexRandom.nextFloat(0.5f, 1f), HexRandom.nextFloat(0.75f, 1.25f));
    }
}
