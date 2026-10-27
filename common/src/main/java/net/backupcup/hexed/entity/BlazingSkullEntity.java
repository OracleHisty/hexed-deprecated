package net.backupcup.hexed.entity;

import net.backupcup.hexed.register.RegisterEntities;
import net.backupcup.hexed.register.RegisterSounds;
import net.backupcup.hexed.util.HexRandom;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Spawned by the Sepulture hex: a flaming skull that homes in on a target and explodes on contact. */
public class BlazingSkullEntity extends FlyingMob implements Enemy {
    private float explosionPower = 1.5f;
    private int ticksUntilNewTarget = 15;
    private int ticksAlive = 0;

    public BlazingSkullEntity(EntityType<? extends FlyingMob> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public BlazingSkullEntity(Level level, Vec3 spawnPos, Vec3 initialVelocity, @Nullable LivingEntity initialTarget, float explosionPower) {
        this(RegisterEntities.BLAZING_SKULL.get(), level);
        this.explosionPower = explosionPower;
        setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        setDeltaMovement(initialVelocity);
        setTarget(initialTarget);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 4.0)
            .add(Attributes.ARMOR, 2.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 4.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    public void tick() {
        super.tick();
        spawnParticles();

        if (level().getGameTime() % HexRandom.nextInt(40, 80) == 0) {
            level().playSound(null, blockPosition(), SoundEvents.BLAZE_AMBIENT, SoundSource.HOSTILE,
                HexRandom.nextFloat(0.25f, 0.5f), HexRandom.nextFloat(1f, 1.5f));
        }

        if (level().isClientSide()) return;

        if (ticksAlive >= 1200) discard();

        if (ticksUntilNewTarget > 0) {
            ticksUntilNewTarget--;
            return;
        }

        ticksAlive++;

        LivingEntity target = getTarget();
        if (target == null) {
            if (HexRandom.nextInt(100) < 5) randomizeDirection();
            else findTarget();
        } else {
            if (!target.canBeSeenAsEnemy() || (target instanceof Player player && player.isCreative())) {
                setTarget(null);
                return;
            }

            if (getBoundingBox().inflate(0.125).intersects(target.getBoundingBox()) && !isDeadOrDying()) {
                explode();
                discard();
                return;
            }

            double range = getAttributeValue(Attributes.FOLLOW_RANGE);
            if (distanceToSqr(target) < range * range) {
                moveTowards(target);
            } else {
                setTarget(null);
            }
        }

        Vec3 velocity = getDeltaMovement();
        setYRot((float) (Mth.atan2(velocity.z, velocity.x) * (180.0 / Math.PI) - 90.0));
    }

    @Override
    protected void onInsideBlock(BlockState state) {
        super.onInsideBlock(state);
        if (!state.isSolid() || level().isClientSide()) return;
        explode();
        discard();
    }

    @Override
    public void die(DamageSource source) {
        explode();
        super.die(source);
    }

    private void explode() {
        level().explode(this, getX(), getY(), getZ(), explosionPower, Level.ExplosionInteraction.NONE);
        level().playSound(null, blockPosition(), RegisterSounds.ACCURSED_ALTAR_HEX.get(), SoundSource.HOSTILE,
            (float) HexRandom.nextDouble(0.5, 1.0), (float) HexRandom.nextDouble(0.75, 1.25));
    }

    private void findTarget() {
        double range = getAttributeValue(Attributes.FOLLOW_RANGE);
        level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(range), entity ->
                entity.canBeSeenAsEnemy() && !(entity instanceof Player player && player.isCreative()) && !(entity instanceof BlazingSkullEntity))
            .stream()
            .min((a, b) -> Double.compare(distanceToSqr(a), distanceToSqr(b)))
            .ifPresent(this::setTarget);
    }

    private void randomizeDirection() {
        Vec3 direction = new Vec3(HexRandom.nextDouble(-10.0, 10.0), HexRandom.nextDouble(-5.0, 5.0), HexRandom.nextDouble(-10.0, 10.0));
        ticksUntilNewTarget = HexRandom.nextInt(20, 60);
        setDeltaMovement(direction.normalize().scale(getAttributeValue(Attributes.MOVEMENT_SPEED)));
    }

    private void moveTowards(LivingEntity target) {
        Vec3 aim = new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ());
        setDeltaMovement(aim.subtract(position()).normalize().scale(getAttributeValue(Attributes.MOVEMENT_SPEED)));
    }

    private void spawnParticles() {
        if (level().getGameTime() % 5 == 0 && HexRandom.nextBoolean()) {
            level().addAlwaysVisibleParticle(ParticleTypes.LAVA, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
            return;
        }
        double x = getX() + HexRandom.nextDouble(-0.5, 0.5);
        double y = getY() + HexRandom.nextDouble(0.0, 1.0);
        double z = getZ() + HexRandom.nextDouble(-0.5, 0.5);
        if (HexRandom.nextBoolean()) {
            level().addAlwaysVisibleParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        } else {
            level().addAlwaysVisibleParticle(HexRandom.nextBoolean() ? ParticleTypes.FLAME : ParticleTypes.SMALL_FLAME, x, y, z, 0.0, 0.0, 0.0);
        }
    }
}
