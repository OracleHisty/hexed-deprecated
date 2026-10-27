package net.backupcup.hexed.item;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.register.RegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

/** The Calamitous robes. Wearing the full set softens most hex downsides. */
public class CalamitousArmorItem extends ArmorItem {
    /**
     * Not registered: the material is only read through the item, and a direct holder works the same on both loaders
     * without depending on registry event order.
     */
    public static final Holder<ArmorMaterial> MATERIAL = Holder.direct(new ArmorMaterial(
        Util.make(new EnumMap<>(Type.class), map -> {
            map.put(Type.HELMET, 1);
            map.put(Type.CHESTPLATE, 4);
            map.put(Type.LEGGINGS, 5);
            map.put(Type.BOOTS, 2);
            map.put(Type.BODY, 4);
        }),
        25,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        () -> Ingredient.of(RegisterItems.CALAMITOUS_FABRIC.get()),
        List.of(new ArmorMaterial.Layer(Hexed.id("calamitous"))),
        0f,
        0f
    ));

    public static final int DURABILITY_MULTIPLIER = 15;

    public CalamitousArmorItem(Type type, Properties properties) {
        super(MATERIAL, type, properties.durability(type.getDurability(DURABILITY_MULTIPLIER)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hexed.calamitous_armor.line_1").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("tooltip.hexed.calamitous_armor.line_2").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("tooltip.hexed.calamitous_armor.line_3").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.hexed.calamitous_armor.line_4").withStyle(ChatFormatting.GRAY));
    }
}
