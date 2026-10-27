package net.backupcup.hexed.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.backupcup.hexed.Config;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.util.HexProjectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Entity implements HexProjectile {
    @Unique private boolean hexed$isVolatile = false;
    @Unique @Nullable private Entity hexed$resentfulOwner = null;
    @Unique @Nullable private UUID hexed$resentfulOwnerId = null;

    protected AbstractArrowMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean hexed$isVolatile() {
        return hexed$isVolatile;
    }

    @Override
    public void hexed$setVolatile(boolean value) {
        hexed$isVolatile = value;
    }

    @Override
    public void hexed$setResentfulOwner(@Nullable Entity owner) {
        hexed$resentfulOwner = owner;
        hexed$resentfulOwnerId = owner != null ? owner.getUUID() : null;
    }

    @Unique
    @Nullable
    private Entity hexed$getResentfulOwner() {
        if (hexed$resentfulOwner == null && hexed$resentfulOwnerId != null) {
            hexed$resentfulOwner = level().getPlayerByUUID(hexed$resentfulOwnerId);
        }
        return hexed$resentfulOwner;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void hexed$writeData(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("isVolatile", hexed$isVolatile);
        if (hexed$resentfulOwnerId != null) tag.putUUID("resentfulOwner", hexed$resentfulOwnerId);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void hexed$readData(CompoundTag tag, CallbackInfo ci) {
        hexed$isVolatile = tag.getBoolean("isVolatile");
        hexed$resentfulOwnerId = tag.hasUUID("resentfulOwner") ? tag.getUUID("resentfulOwner") : null;
        hexed$resentfulOwner = null;
    }

    /** Hooks the arrow's final damage value: Resentful scales it by distance, Volatility blows the arrow up. */
    @ModifyExpressionValue(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;ceil(D)I"))
    private int hexed$onHitDamage(int damage, EntityHitResult hit) {
        Entity target = hit.getEntity();
        Entity owner = hexed$getResentfulOwner();
        if (owner != null && target != null) {
            Config.ResentfulHexParams config = Hexed.config().resentfulHex;
            double distance = target.distanceTo(owner);
            double modifier = distance <= config.increasePoint
                ? 0.3 / config.increasePoint * distance + 0.7
                : Math.min(2.0, 1 / (config.maxIncreaseRange - config.increasePoint + 1) * (distance - config.increasePoint + 1) + 1);
            damage = (int) (damage * modifier);
        }

        if (hexed$isVolatile && !level().isClientSide()) {
            target.level().explode(null, getX(), getY(), getZ(),
                Hexed.config().volatilityHex.explosionPower / 2f, Level.ExplosionInteraction.NONE);
            discard();
        }
        return damage;
    }
}
