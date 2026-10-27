package net.backupcup.hexed.mixin.client;

import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.backupcup.hexed.network.PullPredicatePayload;
import net.backupcup.hexed.util.ItemUseCooldown;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Client half of the charge-based hexes: only the client knows how far a bow is drawn. */
@Mixin(LivingEntity.class)
public abstract class ClientLivingEntityMixin {
    @Unique private static final ResourceLocation PULL = ResourceLocation.withDefaultNamespace("pull");

    @Unique private boolean hexed$holdingCharged = false;
    @Unique private float hexed$storedPredicate = 1f;

    @Unique
    @Nullable
    private LocalPlayer hexed$localPlayer() {
        LocalPlayer player = Minecraft.getInstance().player;
        return (Object) this == player ? player : null;
    }

    /** Whichever held item has a {@code pull} property (a bow or crossbow), and how far it is drawn. */
    @Unique
    private static float hexed$pull(LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        ItemPropertyFunction property = ItemProperties.getProperty(stack, PULL);
        if (property == null) {
            stack = player.getOffhandItem();
            property = ItemProperties.getProperty(stack, PULL);
        }
        return property != null ? property.call(stack, (ClientLevel) player.level(), player, 1234) : 0f;
    }

    /** Celebration and Overclock crossbows fire on their own once fully charged. */
    @Inject(method = "updateUsingItem", at = @At("HEAD"))
    private void hexed$crossbowAutoFire(ItemStack usingStack, CallbackInfo ci) {
        LocalPlayer player = hexed$localPlayer();
        boolean charged = false;

        if (player != null && HexHelper.hasAny(List.of(player.getMainHandItem(), player.getOffhandItem()), Hexes.CELEBRATION, Hexes.OVERCLOCK)
            && hexed$pull(player) >= 1) {
            charged = true;
            Minecraft minecraft = Minecraft.getInstance();
            if (hexed$holdingCharged && minecraft.gameMode != null) {
                minecraft.gameMode.releaseUsingItem(player);
                ((ItemUseCooldown) minecraft).hexed$imposeItemUseCooldown(2);
            }
        }
        hexed$holdingCharged = charged || hexed$holdingCharged;
    }

    @Inject(method = "updatingUsingItem", at = @At("HEAD"))
    private void hexed$sendPullPredicate(CallbackInfo ci) {
        LocalPlayer player = hexed$localPlayer();
        if (player == null) return;

        List<ItemStack> hands = List.of(player.getMainHandItem(), player.getOffhandItem());
        if (!HexHelper.hasAny(hands, Hexes.AGGRAVATE, Hexes.OVERCLOCK, Hexes.PHASED, Hexes.PROVISION, Hexes.VOLATILITY)) return;

        float pull = hexed$pull(player);
        if (player.level().getGameTime() % 5 == 0 && hexed$storedPredicate != pull) {
            hexed$storedPredicate = pull;
            player.connection.send(new ServerboundCustomPayloadPacket(new PullPredicatePayload(pull)));
        }
    }
}
