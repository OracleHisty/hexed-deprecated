package net.backupcup.hexed.register;

import net.backupcup.hexed.block.TallCandle;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.List;

public final class RegisterDecoCandles {
    public record Candle(String name, RegObject<Block> block, Item originalCandle) {}

    public static final List<Candle> CANDLES = List.of(
        candle("tall_candle", MapColor.SAND, Items.CANDLE),
        candle("tall_white_candle", MapColor.SNOW, Items.WHITE_CANDLE),
        candle("tall_light_gray_candle", MapColor.COLOR_LIGHT_GRAY, Items.LIGHT_GRAY_CANDLE),
        candle("tall_gray_candle", MapColor.COLOR_GRAY, Items.GRAY_CANDLE),
        candle("tall_black_candle", MapColor.COLOR_BLACK, Items.BLACK_CANDLE),
        candle("tall_brown_candle", MapColor.COLOR_BROWN, Items.BROWN_CANDLE),
        candle("tall_red_candle", MapColor.COLOR_RED, Items.RED_CANDLE),
        candle("tall_orange_candle", MapColor.COLOR_ORANGE, Items.ORANGE_CANDLE),
        candle("tall_yellow_candle", MapColor.COLOR_YELLOW, Items.YELLOW_CANDLE),
        candle("tall_lime_candle", MapColor.COLOR_LIGHT_GREEN, Items.LIME_CANDLE),
        candle("tall_green_candle", MapColor.COLOR_GREEN, Items.GREEN_CANDLE),
        candle("tall_cyan_candle", MapColor.COLOR_CYAN, Items.CYAN_CANDLE),
        candle("tall_light_blue_candle", MapColor.COLOR_LIGHT_BLUE, Items.LIGHT_BLUE_CANDLE),
        candle("tall_blue_candle", MapColor.COLOR_BLUE, Items.BLUE_CANDLE),
        candle("tall_purple_candle", MapColor.COLOR_PURPLE, Items.PURPLE_CANDLE),
        candle("tall_magenta_candle", MapColor.COLOR_MAGENTA, Items.MAGENTA_CANDLE),
        candle("tall_pink_candle", MapColor.COLOR_PINK, Items.PINK_CANDLE)
    );

    private RegisterDecoCandles() {}

    private static Candle candle(String name, MapColor color, Item originalCandle) {
        RegObject<Block> block = RegisterBlocks.cutout(RegisterBlocks.block(name, () -> new TallCandle(BlockBehaviour.Properties.of()
            .strength(.1f)
            .sound(SoundType.CANDLE)
            .mapColor(color)
            .noOcclusion()
            .lightLevel(TallCandle.STATE_TO_LUMINANCE)
            .pushReaction(PushReaction.DESTROY))));
        return new Candle(name, block, originalCandle);
    }

    public static void init() {}
}
