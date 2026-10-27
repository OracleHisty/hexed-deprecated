package net.backupcup.hexed.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.backupcup.hexed.Config;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.entity.BlazingSkullEntity;
import net.backupcup.hexed.flame.HexedFlames;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.backupcup.hexed.item.BlazingWaveHelper;
import net.backupcup.hexed.item.BlightedHarvestItem;
import net.backupcup.hexed.register.RegisterDamageTypes;
import net.backupcup.hexed.register.RegisterSounds;
import net.backupcup.hexed.register.RegisterStatusEffects;
import net.backupcup.hexed.util.AflameBurning;
import net.backupcup.hexed.util.HexRandom;
import net.backupcup.hexed.util.TimedEvents;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 10)
public abstract class LivingEntityMixin extends Entity implements AflameBurning {
    /** Whether the Aflame effect is active, so clients can draw its flames without knowing the entity's effects. */
    @Unique private static final EntityDataAccessor<Boolean> HEXED$AFLAME = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);

    @Unique private boolean hexed$isFlaring = false;
    @Unique private boolean hexed$hasSpawnedSkulls = false;
    @Unique private int hexed$avertingTicks = 0;

    protected LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow public abstract float getHealth();
    @Shadow public abstract float getMaxHealth();
    @Shadow public abstract boolean hasEffect(Holder<MobEffect> effect);
    @Shadow @Nullable public abstract MobEffectInstance getEffect(Holder<MobEffect> effect);
    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot slot);
    @Shadow public abstract Iterable<ItemStack> getArmorSlots();
    @Shadow public abstract boolean hurt(DamageSource source, float amount);
    @Shadow public abstract boolean isAutoSpinAttack();
    @Shadow public abstract ItemStack getMainHandItem();
    @Shadow protected boolean dead;

    @Unique
    private LivingEntity hexed$self() {
        return (LivingEntity) (Object) this;
    }

    @Override
    public boolean hexed$isAflameBurning() {
        return entityData.get(HEXED$AFLAME);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void hexed$defineAflame(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(HEXED$AFLAME, false);
    }

    /**
     * Keeps the Aflame flag and the flame mod's flame in step with the effect. Runs after movement, because
     * Prometheus resets the fire type of entities that move while not on fire.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void hexed$aflameFlames(CallbackInfo ci) {
        if (level().isClientSide()) return;

        boolean aflame = hasEffect(RegisterStatusEffects.AFLAME.holder());
        boolean wasAflame = entityData.get(HEXED$AFLAME);
        if (aflame != wasAflame) entityData.set(HEXED$AFLAME, aflame);

        if (aflame) {
            // Real fire takes over the flames while it lasts.
            if (getRemainingFireTicks() <= 0) HexedFlames.show(this, HexedFlames.Flame.AFLAME);
        } else if (wasAflame) {
            HexedFlames.clear(this, HexedFlames.Flame.AFLAME);
        }
    }

    /** Aflame ignores effect immunity; only the full Calamitous set keeps it off. */
    @Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
    private void hexed$aflameIgnoresImmunity(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect.is(RegisterStatusEffects.AFLAME.holder())) {
            cir.setReturnValue(!HexHelper.hasFullRobes(getArmorSlots()));
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void hexed$writeData(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("isFlaring", hexed$isFlaring);
        tag.putBoolean("hasSpawnedSkulls", hexed$hasSpawnedSkulls);
        tag.putInt("avertingTicks", hexed$avertingTicks);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void hexed$readData(CompoundTag tag, CallbackInfo ci) {
        hexed$isFlaring = tag.getBoolean("isFlaring");
        hexed$hasSpawnedSkulls = tag.getBoolean("hasSpawnedSkulls");
        hexed$avertingTicks = tag.getInt("avertingTicks");
    }

    @ModifyReturnValue(method = "canDisableShield", at = @At("RETURN"))
    private boolean hexed$harvestDisablesShield(boolean original) {
        return original || getMainHandItem().getItem() instanceof BlightedHarvestItem;
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void hexed$cancelHeal(float amount, CallbackInfo ci) {
        boolean bloodthirsty = HexHelper.has(getItemBySlot(EquipmentSlot.CHEST), Hexes.BLOODTHIRSTY);
        if (hasEffect(RegisterStatusEffects.SMOULDERING.holder())
            || hasEffect(RegisterStatusEffects.TRAITOROUS.holder())
            || hasEffect(RegisterStatusEffects.BLIGHTED.holder())
            || bloodthirsty) {

            // With the full robes, Bloodthirsty only blocks healing above ~77% health.
            if (bloodthirsty && HexHelper.hasFullRobes(getArmorSlots()) && getMaxHealth() / getHealth() > 1.3) return;
            ci.cancel();
        }
    }

    /** Metamorphosis turns healing past full health into food. */
    @Inject(method = "heal", at = @At("HEAD"))
    private void hexed$metamorphosisFood(float amount, CallbackInfo ci) {
        if (getMaxHealth() < getHealth() + amount
            && HexHelper.has(getItemBySlot(EquipmentSlot.HEAD), Hexes.METAMORPHOSIS)
            && hexed$self() instanceof Player player) {
            Config.MetamorphosisHexParams config = Hexed.config().metamorphosisHex;
            float overflow = amount - (getMaxHealth() - getHealth());
            player.getFoodData().eat((int) (overflow * config.foodModifier), config.saturationAmount);
        }
    }

    @ModifyArg(method = "causeFallDamage", index = 1,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private float hexed$fallDamageMultiplier(float amount) {
        if (HexHelper.hasAny(getItemBySlot(EquipmentSlot.FEET), Hexes.AQUATIQUE, Hexes.DYNAMIQUE) && !HexHelper.hasFullRobes(getArmorSlots())) {
            return amount * 1.5f;
        }
        return amount;
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float hexed$persecutedDamage(float amount, DamageSource source) {
        if (source.getDirectEntity() instanceof LivingEntity attacker && HexHelper.has(attacker.getMainHandItem(), Hexes.PERSECUTED)) {
            float healthCap = Hexed.config().persecutedHex.healthCap;
            return amount <= healthCap ? amount * 0.01f * getHealth() : healthCap;
        }
        return amount;
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float hexed$ephemeralDischarge(float amount, DamageSource source) {
        if (source.getDirectEntity() instanceof LivingEntity attacker && HexHelper.has(attacker.getMainHandItem(), Hexes.EPHEMERAL)) {
            MobEffectInstance exhaustion = attacker.getEffect(RegisterStatusEffects.EXHAUSTION.holder());
            if (exhaustion != null) {
                return Math.max(0, amount - exhaustion.getAmplifier());
            }
        }
        return amount;
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float hexed$ironcladDamage(float amount, DamageSource source) {
        if (!HexHelper.has(getItemBySlot(EquipmentSlot.LEGS), Hexes.IRONCLAD)) return amount;

        Config.IroncladHexParams config = Hexed.config().ironcladHex;
        int duration = config.debuffDuration;
        if (HexHelper.hasFullRobes(getArmorSlots())) duration /= Math.max(1, config.robesDebuffModifier);

        if (getHealth() < getMaxHealth() && amount >= 1) {
            HexHelper.multiplyingEffect(hexed$self(), RegisterStatusEffects.IRONCLAD.holder(), duration, config.debuffDecayLength);
        }
        return amount * (1f - config.damageReductionAmount);
    }

    @Unique
    private int hexed$avertingPieces() {
        int pieces = 0;
        for (ItemStack stack : getArmorSlots()) {
            if (HexHelper.has(stack, Hexes.AVERTING)) pieces++;
        }
        return pieces;
    }

    /** Right after Averting dodges a hit, further damage ignores invulnerability frames. */
    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true)
    private DamageSource hexed$avertingSource(DamageSource source) {
        if (hexed$avertingTicks > 0 && hexed$avertingPieces() > 0) {
            return RegisterDamageTypes.of(level(), RegisterDamageTypes.ABYSSAL_CRUSH);
        }
        return source;
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float hexed$avertingDamage(float amount) {
        int pieces = hexed$avertingPieces();
        if (pieces == 0) return amount;

        Config.AvertingHexParams config = Hexed.config().avertingHex;
        if (hexed$avertingTicks == 0 && pieces * config.damageReduction > HexRandom.nextFloat()) {
            hexed$avertingTicks = config.avertingCooldown;
            if (hexed$self() instanceof Player player) {
                player.playNotifySound(RegisterSounds.ACCURSED_ALTAR_TAINT.get(), SoundSource.PLAYERS, 0.25f, 2f);
            }
            return 0f;
        }
        return amount;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void hexed$avertingTick(CallbackInfo ci) {
        if (hexed$avertingTicks > 0) hexed$avertingTicks--;
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float hexed$franticDamage(float amount, DamageSource source) {
        if (source.is(RegisterDamageTypes.FRANTIC_DAMAGE) || !HexHelper.has(getItemBySlot(EquipmentSlot.LEGS), Hexes.FRANTIC)) return amount;

        Config.FranticHexParams config = Hexed.config().franticHex;
        HexHelper.multiplyingEffect(hexed$self(), RegisterStatusEffects.FRANTIC.holder(), config.franticDuration, config.franticDecayLength);

        if (HexHelper.hasFullRobes(getArmorSlots())) return amount;

        // Re-deal the hit as Frantic damage, scaled by how many stacks of Frantic there are.
        MobEffectInstance frantic = getEffect(RegisterStatusEffects.FRANTIC.holder());
        int amplifier = frantic != null ? frantic.getAmplifier() : 0;
        hurt(RegisterDamageTypes.of(level(), RegisterDamageTypes.FRANTIC_DAMAGE), amount * (1 + amplifier / config.damageModifier));
        return 0;
    }

    /** Aquatique boosts water movement efficiency (what Depth Strider uses since 1.21). */
    @WrapOperation(method = "travel",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    private double hexed$aquatiqueSpeed(LivingEntity self, Holder<Attribute> attribute, Operation<Double> original) {
        double value = original.call(self, attribute);
        if (attribute == Attributes.WATER_MOVEMENT_EFFICIENCY && HexHelper.has(getItemBySlot(EquipmentSlot.FEET), Hexes.AQUATIQUE)) {
            // Depth Strider level n is efficiency n/3; Aquatique used to multiply (level + 1).
            return (value * 3 + 1) * Hexed.config().aquatiqueHex.speedMultiplier / 3;
        }
        return value;
    }

    @ModifyReturnValue(method = "getJumpPower()F", at = @At("RETURN"))
    private float hexed$dynamiqueJump(float original) {
        if (HexHelper.has(getItemBySlot(EquipmentSlot.FEET), Hexes.DYNAMIQUE)) {
            return original + Hexed.config().dynamiqueHex.jumpModifier;
        }
        return original;
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void hexed$flaringRiptide(CallbackInfo ci) {
        if (level().isClientSide()) return;

        if (isAutoSpinAttack() && HexHelper.has(getMainHandItem(), Hexes.FLARING)) hexed$isFlaring = true;
        if (!isAutoSpinAttack()) hexed$isFlaring = false;

        if (hexed$isFlaring && level().getEntitiesOfClass(FallingBlockEntity.class, new AABB(blockPosition()).inflate(1)).isEmpty()) {
            FallingBlockEntity.fall(level(), blockPosition(),
                Hexed.config().flaringHex.isSoulFire ? Blocks.SOUL_FIRE.defaultBlockState() : Blocks.FIRE.defaultBlockState());
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void hexed$sepultureRiptide(CallbackInfo ci) {
        if (level().isClientSide()) return;

        if (isAutoSpinAttack() && HexHelper.has(getMainHandItem(), Hexes.SEPULTURE) && !hexed$hasSpawnedSkulls) {
            hexed$hasSpawnedSkulls = true;
            Config.SepultureHexParams config = Hexed.config().sepultureHex;

            for (int i = 0; i < 3; i++) {
                double angle = i * 2 * Math.PI / 3;
                Vec3 spawnPos = new Vec3(getX() + Math.cos(angle), getY() + 0.5, getZ() + Math.sin(angle));
                Vec3 movement = new Vec3((spawnPos.x - getX()) / 4, 0.333, (spawnPos.z - getZ()) / 4);

                LivingEntity target = HexRandom.nextDouble() <= config.angerChance && !HexHelper.hasFullRobes(getArmorSlots())
                    ? hexed$self() : null;
                level().addFreshEntity(new BlazingSkullEntity(level(), spawnPos, movement, target, config.explosionPower));
            }

            level().playSound(null, blockPosition(), RegisterSounds.ACCURSED_ALTAR_HEX.get(), SoundSource.HOSTILE,
                (float) HexRandom.nextDouble(0.5, 1.0), (float) HexRandom.nextDouble(0.75, 1.25));
        }
        if (!isAutoSpinAttack()) hexed$hasSpawnedSkulls = false;
    }

    /** Creatures killed by a blazing wave set off another one a second later. */
    @Inject(method = "die", at = @At("HEAD"))
    private void hexed$blazingBasherChain(DamageSource source, CallbackInfo ci) {
        if (level().isClientSide() || isRemoved() || dead || !source.is(RegisterDamageTypes.BLAZING_WAVE)) return;

        LivingEntity target = hexed$self();
        TimedEvents.schedule(20, () -> BlazingWaveHelper.createExplosion(target, 5.0));
    }
}
