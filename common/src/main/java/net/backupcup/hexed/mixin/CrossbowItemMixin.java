package net.backupcup.hexed.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.backupcup.hexed.Config;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.backupcup.hexed.network.HexUiUpdatePayload;
import net.backupcup.hexed.network.HexedNetwork;
import net.backupcup.hexed.register.RegisterDataComponents;
import net.backupcup.hexed.register.RegisterSounds;
import net.backupcup.hexed.register.RegisterStatusEffects;
import net.backupcup.hexed.util.HexPlayerData;
import net.backupcup.hexed.util.HexRandom;
import net.backupcup.hexed.util.OverclockData;
import net.backupcup.hexed.util.ProvisionData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
    @Unique private static final List<Integer> CELEBRATION_COLORS = List.of(11141120, 11546150, 16351261, 16701501, 8991416, 13061821, 1908001);
    @Unique private static final List<Integer> OVERCLOCK_PINGS = List.of(4, 8, 11, 14, 16, 18, 19, 20);

    /** Celebration loads a random firework in place of each projectile. */
    @Inject(method = "tryLoadProjectiles", at = @At("RETURN"))
    private static void hexed$celebrationFirework(LivingEntity shooter, ItemStack crossbow, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !HexHelper.has(crossbow, Hexes.CELEBRATION)) return;

        ChargedProjectiles loaded = crossbow.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        List<ItemStack> rockets = new ArrayList<>();
        for (int i = 0; i < Math.max(1, loaded.getItems().size()); i++) {
            rockets.add(hexed$randomFirework());
        }
        crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(rockets));
    }

    @Unique
    private static ItemStack hexed$randomFirework() {
        List<FireworkExplosion> explosions = new ArrayList<>();
        int count = HexRandom.nextInt(1, 7);
        for (int i = 0; i < count; i++) {
            List<Integer> palette = new ArrayList<>(CELEBRATION_COLORS);
            Collections.shuffle(palette);
            IntList colors = new IntArrayList(palette.subList(0, Math.min(HexRandom.nextInt(1, 3), palette.size())));
            FireworkExplosion.Shape shape = FireworkExplosion.Shape.byId(HexRandom.nextInt(1, 4));
            explosions.add(new FireworkExplosion(shape, colors, IntList.of(), false, false));
        }

        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        rocket.set(DataComponents.FIREWORKS, new Fireworks(HexRandom.nextInt(0, 2), explosions));
        return rocket;
    }

    /** At high overheat, Overclock has a coin flip chance of failing to load. */
    @ModifyArg(method = "tryLoadProjectiles", index = 1, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/CrossbowItem;draw(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/List;"))
    private static ItemStack hexed$overclockFailedShot(ItemStack crossbow, ItemStack ammo, LivingEntity shooter) {
        if (shooter instanceof ServerPlayer player && HexHelper.has(crossbow, Hexes.OVERCLOCK)
            && ((HexPlayerData) player).hexed$getOverclockData().overheat > 18 && HexRandom.nextDouble() <= 0.5) {
            return ItemStack.EMPTY;
        }
        return ammo;
    }

    /** Celebration fireworks are slower, and sometimes fizzle out right away. */
    @ModifyVariable(method = "performShooting", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float hexed$celebrationVelocity(float velocity, Level level, LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
        if (!HexHelper.has(weapon, Hexes.CELEBRATION)) return velocity;

        Config.CelebrationHexParams config = Hexed.config().celebrationHex;
        if (HexRandom.nextDouble() <= config.faulyChance && !HexHelper.hasFullRobes(shooter)) {
            return config.faultySpeed;
        }
        return velocity - HexRandom.nextFloat(0f, 0.75f);
    }

    @ModifyVariable(method = "performShooting", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private float hexed$crossbowInaccuracy(float inaccuracy, Level level, LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
        if (HexHelper.has(weapon, Hexes.CELEBRATION)) {
            Config.CelebrationHexParams config = Hexed.config().celebrationHex;
            inaccuracy += HexRandom.nextFloat(config.minimumDivergence, config.maximumDivergence);
        }
        if (HexHelper.has(weapon, Hexes.OVERCLOCK) && shooter instanceof ServerPlayer player
            && ((HexPlayerData) player).hexed$getOverclockData().overheat > 16) {
            inaccuracy += (float) HexRandom.nextDouble(-3, 3);
        }
        return inaccuracy;
    }

    @Inject(method = "performShooting", at = @At("TAIL"))
    private void hexed$afterShot(Level level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, float velocity, float inaccuracy,
                                 LivingEntity target, CallbackInfo ci) {
        if (!(shooter instanceof ServerPlayer player)) return;

        // Provision: firing starts the reload minigame.
        if (HexHelper.has(weapon, Hexes.PROVISION)) {
            HexPlayerData data = (HexPlayerData) player;
            data.hexed$setProvisionData(new ProvisionData(true, 0, data.hexed$getProvisionData().reloadSpeed, 0));
        }

        if (HexHelper.has(weapon, Hexes.OVERCLOCK)) {
            hexed$overclockShot(player, weapon);
        }
    }

    /** Each Overclock shot adds a stack of overheat, which charges the crossbow faster but eventually burns the user. */
    @Unique
    private static void hexed$overclockShot(ServerPlayer player, ItemStack weapon) {
        HexPlayerData data = (HexPlayerData) player;
        int overheat = Math.min(20, data.hexed$getOverclockData().overheat + 1);
        data.hexed$setOverclockData(new OverclockData(Hexed.config().overclockHex.overheatLasting, overheat));
        weapon.set(RegisterDataComponents.OVERHEAT.get(), overheat);

        HexedNetwork.sendToPlayer(player, HexUiUpdatePayload.of(HexUiUpdatePayload.Ui.OVERCLOCK, overheat));

        if (overheat > 14) player.igniteForSeconds(overheat);
        if (overheat > 16) {
            player.addEffect(new MobEffectInstance(RegisterStatusEffects.ABLAZE.holder(), overheat * 20, 0, false, false, false));
        }
        if (OVERCLOCK_PINGS.contains(overheat)) {
            player.playNotifySound(RegisterSounds.OVERCLOCK_TIER.get(), SoundSource.PLAYERS, 1f, 1f + overheat / 20f);
        }
    }

    /** Releasing during the Provision minigame reloads instantly; the green zone also speeds up the next one. */
    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void hexed$provisionReload(ItemStack stack, Level level, LivingEntity user, int timeLeft, CallbackInfo ci) {
        if (!(user instanceof ServerPlayer player) || !HexHelper.has(stack, Hexes.PROVISION)) return;

        HexPlayerData playerData = (HexPlayerData) player;
        ProvisionData data = playerData.hexed$getProvisionData();
        if (!data.uiActive) return;

        int pos = data.indicatorPos;
        if (pos >= 17 && pos <= 35) {
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(new ItemStack(Items.ARROW)));
            int reloadSpeed = data.reloadSpeed < Hexed.config().provisionHex.maxReloadSpeed ? data.reloadSpeed + 1 : data.reloadSpeed;
            player.playNotifySound(RegisterSounds.PROVISION_CHARGE.get(), SoundSource.PLAYERS, 0.5f, 0.75f);
            playerData.hexed$setProvisionData(new ProvisionData(false, 0, reloadSpeed, 0));
        } else if ((pos >= 10 && pos <= 16) || (pos >= 36 && pos <= 43)) {
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(new ItemStack(Items.ARROW)));
            player.playNotifySound(RegisterSounds.PROVISION_CHARGE.get(), SoundSource.PLAYERS, 0.5f, 1.25f);
            playerData.hexed$setProvisionData(new ProvisionData(false, 0, data.reloadSpeed, 0));
        } else {
            if (!HexHelper.hasFullRobes(player)) {
                player.getCooldowns().addCooldown(Items.CROSSBOW, Hexed.config().provisionHex.itemCooldown);
            }
            player.playNotifySound(RegisterSounds.PROVISION_FAIL.get(), SoundSource.PLAYERS, 1f, 1f);
            playerData.hexed$setProvisionData(new ProvisionData(false, 0, 1, 0));
        }

        HexedNetwork.sendToPlayer(player, new HexUiUpdatePayload(HexUiUpdatePayload.Ui.PROVISION, false, 0));
    }

    @ModifyReturnValue(method = "getChargeDuration", at = @At("RETURN"))
    private static int hexed$overclockChargeDuration(int original, ItemStack stack, LivingEntity shooter) {
        if (!HexHelper.has(stack, Hexes.OVERCLOCK)) return original;
        return Math.max(1, original - stack.getOrDefault(RegisterDataComponents.OVERHEAT.get(), 0));
    }
}
