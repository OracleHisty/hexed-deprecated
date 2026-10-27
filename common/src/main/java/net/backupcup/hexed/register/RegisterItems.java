package net.backupcup.hexed.register;

import net.backupcup.hexed.item.*;
import net.backupcup.hexed.registry.Reg;
import net.backupcup.hexed.registry.RegObject;
import net.backupcup.hexed.util.TaintedItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public final class RegisterItems {
    private static final List<RegObject<Item>> TAINTED = new ArrayList<>();
    private static final List<RegObject<Item>> RUNES = new ArrayList<>();

    public static final RegObject<Item> CALAMITOUS_FABRIC = item("calamitous_fabric", () -> new TooltipItem(new Item.Properties(), List.of(
        Component.translatable("tooltip.hexed.calamitous_fabric.line_1").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC, ChatFormatting.BOLD),
        Component.translatable("tooltip.hexed.calamitous_fabric.line_2").withStyle(ChatFormatting.GRAY))));

    public static final RegObject<Item> BRIMSTONE_CRYSTAL = item("brimstone_crystal", () -> new TooltipItem(new Item.Properties(), List.of(
        Component.translatable("tooltip.hexed.brimstone_crystal.line_1").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC, ChatFormatting.BOLD),
        Component.translatable("tooltip.hexed.brimstone_crystal.line_2").withStyle(ChatFormatting.GRAY),
        Component.translatable("tooltip.hexed.brimstone_crystal.line_3").withStyle(ChatFormatting.GRAY))));

    //ARMOR
    public static final RegObject<Item> CALAMITOUS_HELMET = item("calamitous_hood", () -> new CalamitousArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegObject<Item> CALAMITOUS_CHESTPLATE = item("calamitous_robes", () -> new CalamitousArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegObject<Item> CALAMITOUS_LEGGINGS = item("calamitous_cassock", () -> new CalamitousArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegObject<Item> CALAMITOUS_BOOTS = item("calamitous_boots", () -> new CalamitousArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    //RUNES, used by the altar renderer
    public static final RegObject<Item> RUNE_AFLAME = rune("aflame");
    public static final RegObject<Item> RUNE_AQUATIQUE = rune("aquatique");
    public static final RegObject<Item> RUNE_AVERTING = rune("averting");
    public static final RegObject<Item> RUNE_BLOODTHIRSTY = rune("bloodthirsty");
    public static final RegObject<Item> RUNE_DISFIGUREMENT = rune("disfigurement");
    public static final RegObject<Item> RUNE_DISPLACED = rune("displaced");
    public static final RegObject<Item> RUNE_DIVINE = rune("divine");
    public static final RegObject<Item> RUNE_DYNAMIQUE = rune("dynamique");
    public static final RegObject<Item> RUNE_EPHEMERAL = rune("ephemeral");
    public static final RegObject<Item> RUNE_EXUBERANCE = rune("exuberance");
    public static final RegObject<Item> RUNE_FRANTIC = rune("frantic");
    public static final RegObject<Item> RUNE_IRONCLAD = rune("ironclad");
    public static final RegObject<Item> RUNE_METAMORPHOSIS = rune("metamorphosis");
    public static final RegObject<Item> RUNE_PERSECUTED = rune("persecuted");
    public static final RegObject<Item> RUNE_TRAITOROUS = rune("traitorous");
    public static final RegObject<Item> RUNE_VINDICTIVE = rune("vindictive");
    public static final RegObject<Item> RUNE_BLIGHTED = rune("blighted");

    //DAIRY
    public static final RegObject<Item> BISHOP_LABRYS = tainted("bishop_labrys", //SMILLY
        () -> new BishopLabrysItem(Tiers.NETHERITE, 5f, -3.2f, "bishop_labrys", new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegObject<Item> BLIGHTED_HARVEST = tainted("blighted_harvest",
        () -> new BlightedHarvestItem(Tiers.NETHERITE, 1, -2f, "blighted_harvest", new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegObject<Item> BLAZING_BASHER = tainted("blazing_basher", //MIRI
        () -> new BlazingBasherItem(Tiers.NETHERITE, 2, -2.4f, "blazing_basher", new Item.Properties().rarity(Rarity.EPIC)));

    //TIMEFALL
    public static final RegObject<Item> BALEFUL_CARNAGE = tainted("baleful_carnage", //PHOENIX
        () -> new BalefulCarnageItem(Tiers.NETHERITE, 3, -2.6f, "baleful_carnage", new Item.Properties().rarity(Rarity.EPIC)));

    private RegisterItems() {}

    private static RegObject<Item> item(String name, Supplier<Item> factory) {
        return Reg.register(BuiltInRegistries.ITEM, name, factory);
    }

    private static RegObject<Item> rune(String name) {
        RegObject<Item> rune = item(name, () -> new Item(new Item.Properties()));
        RUNES.add(rune);
        return rune;
    }

    private static <T extends Item & TaintedItem> RegObject<Item> tainted(String name, Supplier<T> factory) {
        RegObject<Item> item = item(name, factory::get);
        TAINTED.add(item);
        return item;
    }

    public static List<RegObject<Item>> tainted() {
        return Collections.unmodifiableList(TAINTED);
    }

    public static void init() {}
}
