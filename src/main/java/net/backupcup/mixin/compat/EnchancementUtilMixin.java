package net.backupcup.mixin.compat;

import moriyashiine.enchancement.common.util.EnchancementUtil;
import net.backupcup.hexed.register.RegisterTags;
import net.backupcup.hexed.util.TagHelper;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchancementUtil.class)
public class EnchancementUtilMixin {
    @Inject(method = "isEnchantmentAllowed(Lnet/minecraft/util/Identifier;)Z", at = @At("RETURN"), cancellable = true)
    private static void checkIfHasHexEnchantment(Identifier identifier, CallbackInfoReturnable<Boolean> cir) {
        if(TagHelper.isIdentifierInTag(Registries.ENCHANTMENT, RegisterTags.INSTANCE.getHEX_ENCHANTMENTS(), identifier)) cir.setReturnValue(false);
    }
}
