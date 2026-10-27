package net.backupcup.hexed.mixin;

import net.backupcup.hexed.Config;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.backupcup.hexed.register.RegisterStatusEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** Divine: opening a loot chest rolls its loot twice, at the cost of a random debuff. */
@Mixin(LootTable.class)
public abstract class LootTableMixin {
    @Unique private static final ThreadLocal<Boolean> HEXED$REFILLING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "fill", at = @At("TAIL"))
    private void hexed$divineLoot(Container container, LootParams params, long seed, CallbackInfo ci) {
        if (HEXED$REFILLING.get()) return;
        if (!(params.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player)) return;
        if (!HexHelper.has(player.getItemBySlot(EquipmentSlot.HEAD), Hexes.DIVINE)) return;

        HEXED$REFILLING.set(true);
        try {
            ((LootTable) (Object) this).fill(container, params, seed);
        } finally {
            HEXED$REFILLING.set(false);
        }

        Config.DivineHexParams config = Hexed.config().divineHex;
        List<Holder<MobEffect>> debuffs = List.of(
            RegisterStatusEffects.ABLAZE.holder(),
            RegisterStatusEffects.ETHEREAL.holder(),
            RegisterStatusEffects.EXHAUSTION.holder(),
            RegisterStatusEffects.IRONCLAD.holder(),
            RegisterStatusEffects.TRAITOROUS.holder());
        Holder<MobEffect> debuff = debuffs.get(player.getRandom().nextInt(debuffs.size()));

        int amplifier = config.defaultAmplifier;
        int seconds = config.defaultDuration;
        if (debuff == RegisterStatusEffects.IRONCLAD.holder()) {
            amplifier = config.ironcladAmplifier;
            seconds = config.ironcladDuration;
        } else if (debuff == RegisterStatusEffects.EXHAUSTION.holder()) {
            amplifier = config.exhaustionAmplifier;
            seconds = config.exhaustionDuration;
        } else if (debuff == RegisterStatusEffects.ETHEREAL.holder()) {
            amplifier = config.etherealAmplifier;
            seconds = config.etherealDuration;
        }
        if (HexHelper.hasFullRobes(player)) seconds /= 2;

        player.addEffect(new MobEffectInstance(debuff, 20 * seconds, amplifier, true, false, true));
    }
}
