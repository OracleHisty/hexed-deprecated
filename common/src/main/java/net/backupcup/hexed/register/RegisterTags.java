package net.backupcup.hexed.register;

import net.backupcup.hexed.Hexed;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

public final class RegisterTags {
    public static final TagKey<Enchantment> HEX_ENCHANTMENTS = TagKey.create(Registries.ENCHANTMENT, Hexed.id("hex_enchantments"));
    public static final TagKey<MobEffect> PERSISTENT_DEBUFFS = TagKey.create(Registries.MOB_EFFECT, Hexed.id("persistent_debuffs"));
    public static final TagKey<Item> CALAMITOUS_ARMOR = TagKey.create(Registries.ITEM, Hexed.id("calamitous_armor"));

    private RegisterTags() {}
}
