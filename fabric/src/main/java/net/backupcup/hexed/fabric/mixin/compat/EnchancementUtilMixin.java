package net.backupcup.hexed.fabric.mixin.compat;

import moriyashiine.enchancement.common.util.EnchancementUtil;
import net.backupcup.hexed.hex.Hexes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Enchancement strips enchantments missing from its allow list; hexes are always allowed. */
@Mixin(EnchancementUtil.class)
public abstract class EnchancementUtilMixin {
    @Inject(method = "isEnchantmentAllowed(Lnet/minecraft/resources/ResourceLocation;)Z", at = @At("RETURN"), cancellable = true)
    private static void hexed$allowHexes(ResourceLocation id, CallbackInfoReturnable<Boolean> cir) {
        if (Hexes.isHex(ResourceKey.create(Registries.ENCHANTMENT, id))) cir.setReturnValue(true);
    }
}
