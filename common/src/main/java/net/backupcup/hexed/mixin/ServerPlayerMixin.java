package net.backupcup.hexed.mixin;

import com.mojang.authlib.GameProfile;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.backupcup.hexed.network.HexUiUpdatePayload;
import net.backupcup.hexed.network.HexedNetwork;
import net.backupcup.hexed.register.RegisterSounds;
import net.backupcup.hexed.util.AggravateData;
import net.backupcup.hexed.util.HexPlayerData;
import net.backupcup.hexed.util.OverclockData;
import net.backupcup.hexed.util.ProvisionData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements HexPlayerData {
    @Unique private static final int PROVISION_INDICATOR_LIMIT = 78;

    @Unique private float hexed$pullPredicate = 0;
    @Unique private ProvisionData hexed$provisionData = new ProvisionData(false, 0, 1, 0);
    @Unique private AggravateData hexed$aggravateData = new AggravateData(InteractionHand.MAIN_HAND, 0, 0);
    @Unique private OverclockData hexed$overclockData = new OverclockData(0, 0);
    @Unique private int hexed$phasedCharge = 0;

    protected ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile profile) {
        super(level, pos, yRot, profile);
    }

    @Override
    public float hexed$getPullPredicate() {
        return hexed$pullPredicate;
    }

    @Override
    public void hexed$setPullPredicate(float value) {
        hexed$pullPredicate = value;
    }

    @Override
    public ProvisionData hexed$getProvisionData() {
        return hexed$provisionData;
    }

    @Override
    public void hexed$setProvisionData(ProvisionData data) {
        hexed$provisionData = data;
    }

    @Override
    public AggravateData hexed$getAggravateData() {
        return hexed$aggravateData;
    }

    @Override
    public void hexed$setAggravateData(AggravateData data) {
        hexed$aggravateData = data;
    }

    @Override
    public OverclockData hexed$getOverclockData() {
        return hexed$overclockData;
    }

    @Override
    public void hexed$setOverclockData(OverclockData data) {
        hexed$overclockData = data;
    }

    @Override
    public int hexed$getPhasedCharge() {
        return hexed$phasedCharge;
    }

    @Override
    public void hexed$setPhasedCharge(int value) {
        hexed$phasedCharge = value;
    }

    @Unique
    private ServerPlayer hexed$self() {
        return (ServerPlayer) (Object) this;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void hexed$readData(CompoundTag tag, CallbackInfo ci) {
        CompoundTag overclock = tag.getCompound("OverclockData");
        hexed$overclockData.cooldown = overclock.getInt("OverclockCooldown");
        hexed$overclockData.overheat = overclock.getInt("OverclockOverheat");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void hexed$writeData(CompoundTag tag, CallbackInfo ci) {
        CompoundTag overclock = new CompoundTag();
        overclock.putInt("OverclockCooldown", hexed$overclockData.cooldown);
        overclock.putInt("OverclockOverheat", hexed$overclockData.overheat);
        tag.put("OverclockData", overclock);
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void hexed$resetClientDataOnDeath(DamageSource source, CallbackInfo ci) {
        for (HexUiUpdatePayload.Ui ui : HexUiUpdatePayload.Ui.values()) {
            HexedNetwork.sendToPlayer(hexed$self(), new HexUiUpdatePayload(ui, false, 0));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void hexed$playerTick(CallbackInfo ci) {
        hexed$tickProvision();
        hexed$tickOverclock();
        hexed$tickAggravate();
    }

    /** Moves the Provision reload indicator along while the reload minigame is running. */
    @Unique
    private void hexed$tickProvision() {
        ProvisionData data = hexed$provisionData;
        if (!data.uiActive) return;

        if (data.indicatorPos >= PROVISION_INDICATOR_LIMIT) {
            data.uiActive = false;
            if (!HexHelper.hasFullRobes(this)) {
                getCooldowns().addCooldown(Items.CROSSBOW, Hexed.config().provisionHex.itemCooldown);
            }
        } else {
            data.indicatorPos += data.reloadSpeed;
        }

        if (data.indicatorPos >= 10 && data.indicatorPos <= 16 && data.reloadsPlayed < 1) {
            playNotifySound(RegisterSounds.PROVISION_IN_RANGE.get(), SoundSource.PLAYERS, 0.5f, 1f);
            data.reloadsPlayed = 1;
        }
        if (data.indicatorPos >= 36 && data.indicatorPos <= 43 && data.reloadsPlayed < 2) {
            playNotifySound(RegisterSounds.PROVISION_IN_RANGE.get(), SoundSource.PLAYERS, 0.5f, 1f);
            data.reloadsPlayed = 2;
        }

        HexedNetwork.sendToPlayer(hexed$self(), new HexUiUpdatePayload(HexUiUpdatePayload.Ui.PROVISION,
            data.indicatorPos <= PROVISION_INDICATOR_LIMIT && data.uiActive, data.indicatorPos));

        if (data.indicatorPos > PROVISION_INDICATOR_LIMIT) {
            data.uiActive = false;
            data.indicatorPos = 0;
            data.reloadSpeed = 1;
            data.reloadsPlayed = 0;
            playNotifySound(RegisterSounds.PROVISION_FAIL.get(), SoundSource.PLAYERS, 1f, 1f);
        }
    }

    /** Overheat cools down by one stack every half second once the cooldown has run out. */
    @Unique
    private void hexed$tickOverclock() {
        OverclockData data = hexed$overclockData;
        if (data.cooldown > 0) {
            data.cooldown--;
        } else if (level().getGameTime() % 10L == 0L && data.overheat > 0) {
            data.overheat--;
            HexedNetwork.sendToPlayer(hexed$self(), HexUiUpdatePayload.of(HexUiUpdatePayload.Ui.OVERCLOCK, data.overheat));
        }
    }

    /** Builds up Aggravate charges while a fully drawn bow is held. */
    @Unique
    private void hexed$tickAggravate() {
        AggravateData data = hexed$aggravateData;
        boolean holdingAggravate = HexHelper.has(getItemInHand(data.bowHand), Hexes.AGGRAVATE);

        if (holdingAggravate && hexed$pullPredicate >= 1) {
            int chargeTicks = Hexed.config().aggravateHex.chargeTicks;
            if (data.chargeAmount < 7) data.chargedTicks++;

            if (data.chargedTicks >= chargeTicks && data.chargeAmount < 6) {
                data.chargedTicks = 0;
                data.chargeAmount++;
                HexedNetwork.sendToPlayer(hexed$self(), HexUiUpdatePayload.of(HexUiUpdatePayload.Ui.AGGRAVATE, data.chargeAmount));
                playNotifySound(RegisterSounds.AGGRAVATE_TIER.get(), SoundSource.PLAYERS, 1f, 1f + data.chargeAmount / 6f);
            } else if (data.chargedTicks >= chargeTicks * 10L && data.chargeAmount == 6) {
                // Holding a full charge for too long overcharges the bow.
                data.chargedTicks = 0;
                data.chargeAmount = 7;
                HexedNetwork.sendToPlayer(hexed$self(), HexUiUpdatePayload.of(HexUiUpdatePayload.Ui.AGGRAVATE, data.chargeAmount));
                playNotifySound(RegisterSounds.ACCURSED_ALTAR_TAINT.get(), SoundSource.PLAYERS, 0.5f, 2f);
            }
        }
        if (!holdingAggravate && data.chargeAmount > 0) {
            HexedNetwork.sendToPlayer(hexed$self(), HexUiUpdatePayload.of(HexUiUpdatePayload.Ui.AGGRAVATE, 0));
        }
    }
}
