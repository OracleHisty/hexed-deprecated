package net.backupcup.hexed.register;

import net.backupcup.hexed.altar.AccursedAltarBlock;
import net.backupcup.hexed.block.AbstractTallCandle;
import net.backupcup.hexed.block.BrimstoneCandle;
import net.backupcup.hexed.block.LichloreCandle;
import net.backupcup.hexed.registry.Reg;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public final class RegisterBlocks {
    private static final List<RegObject<? extends Block>> CUTOUT = new ArrayList<>();

    public static final RegObject<Block> BRIMSTONE_CANDLE = cutout(block("brimstone_candle", () -> new BrimstoneCandle(BlockBehaviour.Properties.of()
        .strength(.1f)
        .sound(SoundType.CANDLE)
        .mapColor(MapColor.FIRE)
        .noOcclusion()
        .lightLevel(AbstractTallCandle.STATE_TO_LUMINANCE)
        .pushReaction(PushReaction.DESTROY))));

    public static final RegObject<Block> LICHLORE_CANDLE = cutout(block("lichlore_candle", () -> new LichloreCandle(BlockBehaviour.Properties.of()
        .strength(.1f)
        .sound(SoundType.CANDLE)
        .mapColor(MapColor.PLANT)
        .noOcclusion()
        .lightLevel(AbstractTallCandle.STATE_TO_LUMINANCE)
        .pushReaction(PushReaction.DESTROY))));

    public static final RegObject<Block> ACCURSED_ALTAR = cutout(block("accursed_altar", () -> new AccursedAltarBlock(BlockBehaviour.Properties.of()
        .strength(3.5f)
        .sound(SoundType.ANCIENT_DEBRIS)
        .mapColor(MapColor.CRIMSON_HYPHAE)
        .noOcclusion()
        .pushReaction(PushReaction.BLOCK))));

    private RegisterBlocks() {}

    public static <T extends Block> RegObject<T> block(String name, Supplier<T> factory) {
        return block(name, factory, Item.Properties::new);
    }

    public static <T extends Block> RegObject<T> block(String name, Supplier<T> factory, Supplier<Item.Properties> itemProperties) {
        RegObject<T> block = Reg.register(BuiltInRegistries.BLOCK, name, factory);
        Reg.register(BuiltInRegistries.ITEM, name, () -> new BlockItem(block.get(), itemProperties.get()));
        return block;
    }

    /** Marks a block as rendering with the cutout layer. */
    public static <T extends RegObject<? extends Block>> T cutout(T block) {
        CUTOUT.add(block);
        return block;
    }

    public static List<RegObject<? extends Block>> cutoutBlocks() {
        return Collections.unmodifiableList(CUTOUT);
    }

    public static void init() {
        RegisterSlagBlocks.init();
        RegisterDecoCandles.init();
        RegisterPlushies.init();
    }
}
