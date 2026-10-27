package net.backupcup.hexed.register;

import net.backupcup.hexed.platform.Services;
import net.backupcup.hexed.registry.Reg;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class RegisterCreativeTabs {
    public static final RegObject<CreativeModeTab> MAIN = Reg.register(BuiltInRegistries.CREATIVE_MODE_TAB, "hexed_group", () ->
        Services.PLATFORM.creativeTabBuilder()
            .title(Component.translatable("itemGroup.hexed.hexed_group"))
            .icon(() -> new ItemStack(RegisterItems.BRIMSTONE_CRYSTAL.get()))
            .displayItems((parameters, output) -> {
                output.accept(RegisterItems.BRIMSTONE_CRYSTAL.get());
                output.accept(RegisterItems.CALAMITOUS_FABRIC.get());

                output.accept(RegisterItems.CALAMITOUS_HELMET.get());
                output.accept(RegisterItems.CALAMITOUS_CHESTPLATE.get());
                output.accept(RegisterItems.CALAMITOUS_LEGGINGS.get());
                output.accept(RegisterItems.CALAMITOUS_BOOTS.get());

                output.accept(RegisterBlocks.ACCURSED_ALTAR.get());

                output.accept(RegisterBlocks.BRIMSTONE_CANDLE.get());
                output.accept(RegisterBlocks.LICHLORE_CANDLE.get());
                output.accept(RegisterItems.BRIMSTONE_FLINT_AND_STEEL.get());

                for (RegObject<Item> tainted : RegisterItems.tainted()) {
                    output.accept(tainted.get());
                }
            })
            .build());

    public static final RegObject<CreativeModeTab> DECO = Reg.register(BuiltInRegistries.CREATIVE_MODE_TAB, "hexed_deco_group", () ->
        Services.PLATFORM.creativeTabBuilder()
            .title(Component.translatable("itemGroup.hexed.hexed_deco_group"))
            .icon(() -> new ItemStack(RegisterSlagBlocks.BRIMSTONE_SLAG_PILLAR.get()))
            .displayItems((parameters, output) -> {
                output.accept(RegisterSlagBlocks.LAVENDIN_CINDER.get());
                output.accept(RegisterSlagBlocks.LAVENDIN_VERDURE.get());
                output.accept(RegisterSlagBlocks.LAVA_PISTIL.get());
                output.accept(RegisterSlagBlocks.BLAZING_MAGMA_BUCKET.get());

                output.accept(RegisterSlagBlocks.BRIMSTONE_SLAG.get());
                output.accept(RegisterSlagBlocks.BRIMSTONE_BRICKS_STAIRS.get());
                output.accept(RegisterSlagBlocks.BRIMSTONE_BRICKS_SLAB.get());
                output.accept(RegisterSlagBlocks.BRIMSTONE_BRICKS_WALL.get());

                output.accept(RegisterSlagBlocks.BRIMSTONE_BRICKS.get());
                output.accept(RegisterSlagBlocks.BRIMSTONE_SLAG_STAIRS.get());
                output.accept(RegisterSlagBlocks.BRIMSTONE_SLAG_SLAB.get());
                output.accept(RegisterSlagBlocks.BRIMSTONE_SLAG_WALL.get());

                output.accept(RegisterSlagBlocks.SMOOTH_BRIMSTONE_SLAG.get());
                output.accept(RegisterSlagBlocks.SMOOTH_BRIMSTONE_SLAG_STAIRS.get());
                output.accept(RegisterSlagBlocks.SMOOTH_BRIMSTONE_SLAG_SLAB.get());
                output.accept(RegisterSlagBlocks.SMOOTH_BRIMSTONE_SLAG_WALL.get());

                output.accept(RegisterSlagBlocks.BRIMSTONE_SLAG_PILLAR.get());
                output.accept(RegisterSlagBlocks.CHISELED_BRIMSTONE_SLAG.get());

                for (RegisterDecoCandles.Candle candle : RegisterDecoCandles.CANDLES) {
                    output.accept(candle.block().get());
                }
            })
            .build());

    public static final RegObject<CreativeModeTab> PLUSHIES = Reg.register(BuiltInRegistries.CREATIVE_MODE_TAB, "hexed_plushie_group", () ->
        Services.PLATFORM.creativeTabBuilder()
            .title(Component.translatable("itemGroup.hexed.hexed_plushie_group"))
            .icon(() -> new ItemStack(RegisterPlushies.CALAMAIDAS_PLUSHIE.get()))
            .displayItems((parameters, output) -> {
                for (RegObject<Block> plushie : RegisterPlushies.all()) {
                    output.accept(plushie.get());
                }
            })
            .build());

    private RegisterCreativeTabs() {}

    public static void init() {}
}
