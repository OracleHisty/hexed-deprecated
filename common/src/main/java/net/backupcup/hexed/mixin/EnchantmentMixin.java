package net.backupcup.hexed.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.backupcup.hexed.hex.HexHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {
    /** Hexes aren't curses, so vanilla would list them in gray. They get their own color instead. */
    @ModifyReturnValue(method = "getFullname", at = @At("RETURN"))
    private static Component hexed$hexColor(Component original, Holder<Enchantment> enchantment, int level) {
        if (!HexHelper.isHex(enchantment)) return original;
        return original.copy().withStyle(HexHelper.HEX_COLOR);
    }
}
