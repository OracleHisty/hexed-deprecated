package net.backupcup.hexed.mixin;

import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.backupcup.hexed.item.BlightedHarvestItem;
import net.backupcup.hexed.register.RegisterStatusEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Player.class, priority = 10)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow @Final private ItemCooldowns cooldowns;

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    /** Disabling a shield with the Harvest of the Blighted blights the attacker for one attack cycle. */
    @Inject(method = "blockUsingShield",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;disableShield()V", shift = At.Shift.AFTER))
    private void hexed$harvestApplyBlight(LivingEntity attacker, CallbackInfo ci) {
        if (attacker.getMainHandItem().getItem() instanceof BlightedHarvestItem harvest) {
            attacker.addEffect(new MobEffectInstance(RegisterStatusEffects.BLIGHTED.holder(),
                harvest.attackCooldownTicks(), 0, false, true, true));
        }
    }

    /** Hex cooldowns (Traitorous, Phased, ...) also stop melee attacks. */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void hexed$cancelCooldownAttack(Entity target, CallbackInfo ci) {
        for (ItemStack stack : getHandSlots()) {
            if (cooldowns.isOnCooldown(stack.getItem())) {
                ci.cancel();
                return;
            }
        }
    }

    /** Metamorphosis stops natural regeneration (food heals through overflow instead). */
    @Inject(method = "isHurt", at = @At("HEAD"), cancellable = true)
    private void hexed$naturalRegen(CallbackInfoReturnable<Boolean> cir) {
        if (HexHelper.has(getItemBySlot(EquipmentSlot.HEAD), Hexes.METAMORPHOSIS) && !HexHelper.hasFullRobes(getArmorSlots())) {
            cir.setReturnValue(false);
        }
    }
}
