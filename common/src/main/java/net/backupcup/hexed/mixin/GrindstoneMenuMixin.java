package net.backupcup.hexed.mixin;

import net.backupcup.hexed.hex.HexHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/** The grindstone only keeps curses. Hexes aren't curses, but they stay on the item the same way. */
@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin {
    @ModifyArg(method = "removeNonCursesFrom",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;updateEnchantments(Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/world/item/enchantment/ItemEnchantments;"),
        index = 1)
    private Consumer<ItemEnchantments.Mutable> hexed$keepHexes(Consumer<ItemEnchantments.Mutable> removeNonCurses) {
        return mutable -> {
            Map<Holder<Enchantment>, Integer> hexes = new LinkedHashMap<>();
            for (Holder<Enchantment> enchantment : mutable.keySet()) {
                if (HexHelper.isHex(enchantment)) hexes.put(enchantment, mutable.getLevel(enchantment));
            }
            removeNonCurses.accept(mutable);
            hexes.forEach(mutable::set);
        };
    }
}
