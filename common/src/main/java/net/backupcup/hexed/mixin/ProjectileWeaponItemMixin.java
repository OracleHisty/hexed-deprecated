package net.backupcup.hexed.mixin;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.backupcup.hexed.util.HexPlayerData;
import net.backupcup.hexed.util.HexProjectile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Bow hexes that change the arrows being fired. */
@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponItemMixin {
    @Inject(method = "createProjectile", at = @At("RETURN"))
    private void hexed$bowProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit,
                                     CallbackInfoReturnable<Projectile> cir) {
        if (!((Object) this instanceof BowItem) || !(shooter instanceof ServerPlayer player)) return;
        if (!(cir.getReturnValue() instanceof HexProjectile projectile)) return;

        // Volatility: a full draw makes the arrow explode on hit; anything less blows up in the archer's face.
        if (HexHelper.has(weapon, Hexes.VOLATILITY)) {
            float pull = ((HexPlayerData) player).hexed$getPullPredicate();
            if (pull == 1f || HexHelper.hasFullRobes(player)) {
                projectile.hexed$setVolatile(true);
            } else {
                level.explode(null, player.getX(), player.getBoundingBox().getCenter().y, player.getZ(),
                    Hexed.config().volatilityHex.explosionPower - pull, Level.ExplosionInteraction.NONE);
            }
        }

        if (HexHelper.has(weapon, Hexes.RESENTFUL)) {
            projectile.hexed$setResentfulOwner(player);
        }
    }
}
