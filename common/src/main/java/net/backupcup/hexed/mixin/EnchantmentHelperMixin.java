package net.backupcup.hexed.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.hex.HexEffects;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    /** Every melee and projectile hit ends up here, for both the attacker's and the victim's enchantments. */
    @Inject(method = "doPostAttackEffectsWithItemSource", at = @At("HEAD"))
    private static void hexed$hexPostAttack(ServerLevel level, Entity target, DamageSource source, @Nullable ItemStack itemSource, CallbackInfo ci) {
        HexEffects.onPostAttack(level, target, source);
    }

    /** Ephemeral adds flat damage. Its {@code minecraft:damage} effect can't read the config, so it's added here. */
    @ModifyReturnValue(method = "modifyDamage", at = @At("RETURN"))
    private static float hexed$ephemeralDamage(float original, ServerLevel level, ItemStack weapon, Entity target, DamageSource source, float baseDamage) {
        if (HexHelper.has(weapon, Hexes.EPHEMERAL)) {
            return original + Hexed.config().ephemeralHex.damageAddition;
        }
        return original;
    }
}
