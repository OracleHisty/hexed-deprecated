package net.backupcup.hexed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.backupcup.hexed.hex.HexHelper;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** The grindstone's result slot. Hexes it keeps on the item give no experience, like curses. */
@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
public abstract class GrindstoneResultSlotMixin {
    @WrapOperation(method = "getExperienceFromItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean hexed$noHexExperience(Holder<Enchantment> enchantment, TagKey<Enchantment> tag, Operation<Boolean> original) {
        return original.call(enchantment, tag) || (tag == EnchantmentTags.CURSE && HexHelper.isHex(enchantment));
    }
}
