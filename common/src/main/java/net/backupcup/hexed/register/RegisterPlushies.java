package net.backupcup.hexed.register;

import net.backupcup.hexed.block.PlushieBlock;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public final class RegisterPlushies {
    private static final List<RegObject<Block>> PLUSHIES = new ArrayList<>();
    private static final List<RegObject<Block>> SPECIAL = new ArrayList<>();

    //heheheha
    public static final RegObject<Block> CALAMAIDAS_PLUSHIE = special("calamaidas_plushie", "tooltip.hexed.calamaidas_plushie", RegisterSounds.ACCURSED_ALTAR_ACTIVATE, MapColor.WOOL);

    //thumbsup people
    public static final RegObject<Block> BON_PLUSHIE = special("bonfire_plushie", "tooltip.hexed.bonfire_plushie", () -> SoundEvents.CAMPFIRE_CRACKLE, MapColor.COLOR_ORANGE);
    public static final RegObject<Block> MIRI_PLUSHIE = special("yirmiri_plushie", "tooltip.hexed.miri_plushie", RegisterSounds.TECH_LAUGH, MapColor.GOLD);
    public static final RegObject<Block> SMILLY_PLUSHIE = special("smilly_plushie", "tooltip.hexed.smilly_plushie", null, MapColor.COLOR_BLACK);

    static {
        simple("chronos_plushie", MapColor.COLOR_GREEN);
        simple("maggie_plushie", MapColor.FIRE);
        simple("zarra_plushie", MapColor.COLOR_LIGHT_GREEN);
        simple("wagon_plushie", MapColor.COLOR_LIGHT_BLUE);
        simple("josh_plushie", MapColor.SNOW);
        simple("solace_plushie", MapColor.CRIMSON_STEM);
        simple("nightfall_solace_plushie", MapColor.COLOR_PURPLE);
        simple("phoenix_plushie", MapColor.COLOR_BLACK);
        simple("tiger_plushie", MapColor.DIAMOND);
        simple("eternal_plushie", MapColor.COLOR_BLUE);
        simple("tollish_plushie", MapColor.COLOR_PINK);
        simple("crafter_plushie", MapColor.TERRACOTTA_BROWN);
        simple("den_plushie", MapColor.COLOR_BLACK);
        simple("fzzy_plushie", MapColor.COLOR_BROWN);
        simple("kami_plushie", MapColor.COLOR_BLACK);
        simple("gobby_plushie", MapColor.CRIMSON_STEM);
        simple("silver_plushie", MapColor.COLOR_YELLOW);
        simple("bread_plushie", MapColor.COLOR_BROWN);
        simple("assistance_plushie", MapColor.COLOR_PINK);
        simple("monk_plushie", MapColor.COLOR_GREEN);
        simple("tenno_plushie", MapColor.COLOR_BLACK);
        simple("toboe_plushie", MapColor.COLOR_BLACK);
        simple("reader_plushie", MapColor.COLOR_BLACK);
    }

    private static final float CHEST_CHANCE = 0.3125f;

    private RegisterPlushies() {}

    private static BlockBehaviour.Properties plushie(MapColor color) {
        return BlockBehaviour.Properties.of()
            .strength(1f)
            .sound(SoundType.WOOL)
            .mapColor(color)
            .noOcclusion()
            .pushReaction(PushReaction.NORMAL);
    }

    private static RegObject<Block> register(String name, Supplier<Block> factory, Rarity rarity) {
        RegObject<Block> block = RegisterBlocks.cutout(RegisterBlocks.block(name, factory, () -> new Item.Properties().rarity(rarity)));
        PLUSHIES.add(block);
        return block;
    }

    private static RegObject<Block> special(String name, String description, @Nullable Supplier<SoundEvent> sound, MapColor color) {
        RegObject<Block> block = register(name, () -> new PlushieBlock(plushie(color), sound, description), Rarity.EPIC);
        SPECIAL.add(block);
        return block;
    }

    private static void simple(String name, MapColor color) {
        register(name, () -> new PlushieBlock(plushie(color), null, null), Rarity.UNCOMMON);
    }

    public static List<RegObject<Block>> all() {
        return Collections.unmodifiableList(PLUSHIES);
    }

    /** The pool added to every chest loot table. Special plushies are rarer than the rest. */
    public static LootPool.Builder chestPool() {
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f));
        for (RegObject<Block> plushie : PLUSHIES) {
            pool.when(LootItemRandomChanceCondition.randomChance(SPECIAL.contains(plushie) ? CHEST_CHANCE : CHEST_CHANCE * 8))
                .add(LootItem.lootTableItem(plushie.get()));
        }
        return pool;
    }

    public static void init() {}
}
