package net.backupcup.mixin.compat;

import moriyashiine.enchancement.common.ModConfig;
import moriyashiine.enchancement.common.util.EnchancementUtil;
import net.backupcup.hexed.enchantments.AbstractHex;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchancementUtil.class)
public class EnchancementUtilMixin {
    @Inject(method = "isEnchantmentAllowed(Lnet/minecraft/util/Identifier;)Z", at = @At("RETURN"), remap = false, cancellable = true)
    private static void checkIfHasHexEhnchantment(Identifier identifier, CallbackInfoReturnable<Boolean> cir) {
        if(Registries.ENCHANTMENT.get(identifier) instanceof AbstractHex) cir.setReturnValue(false);
    }
}
