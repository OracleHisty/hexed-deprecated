package net.backupcup.hexed.register;

import net.backupcup.hexed.block.*;
import net.backupcup.hexed.platform.Services;
import net.backupcup.hexed.registry.Reg;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class RegisterSlagBlocks {
    public static final RegObject<FlowingFluid> BLAZING_MAGMA_SOURCE = Reg.register(BuiltInRegistries.FLUID, "blazing_magma",
        Services.PLATFORM::createBlazingMagmaSource);
    public static final RegObject<FlowingFluid> FLOWING_BLAZING_MAGMA = Reg.register(BuiltInRegistries.FLUID, "flowing_blazing_magma",
        Services.PLATFORM::createBlazingMagmaFlowing);

    public static final RegObject<Block> CHISELED_BRIMSTONE_SLAG = RegisterBlocks.block("chiseled_brimstone_slag", () -> new Block(slag()));
    public static final RegObject<Block> BRIMSTONE_SLAG_PILLAR = RegisterBlocks.cutout(RegisterBlocks.block("brimstone_slag_pillar", () -> new BrimstoneSlagPillar(slag().noOcclusion())));
    public static final RegObject<Block> LAVENDIN_CINDER = RegisterBlocks.block("lavendin_cinder", () -> new LavendinCinderBlock(slag().mapColor(MapColor.COLOR_PINK)));
    public static final RegObject<Block> LAVENDIN_VERDURE = RegisterBlocks.cutout(RegisterBlocks.block("lavendin_verdure", () -> new LavendinVerdureBlock(BlockBehaviour.Properties.of()
        .strength(0f)
        .sound(SoundType.GRASS)
        .mapColor(MapColor.COLOR_PINK)
        .pushReaction(PushReaction.DESTROY)
        .noOcclusion().forceSolidOff().noCollission())));
    public static final RegObject<Block> LAVA_PISTIL = RegisterBlocks.cutout(RegisterBlocks.block("lava_pistil", () -> new PistilBlock(BlockBehaviour.Properties.of()
        .strength(0f)
        .sound(SoundType.FROGLIGHT)
        .mapColor(MapColor.COLOR_ORANGE)
        .noOcclusion().noCollission().forceSolidOff()
        .lightLevel(PistilBlock.STATE_TO_LUMINANCE))));
    public static final RegObject<LiquidBlock> BLAZING_MAGMA = Reg.register(BuiltInRegistries.BLOCK, "blazing_magma",
        () -> new LiquidBlock(BLAZING_MAGMA_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA)));
    public static final RegObject<Item> BLAZING_MAGMA_BUCKET = Reg.register(BuiltInRegistries.ITEM, "blazing_magma_bucket",
        () -> new BucketItem(BLAZING_MAGMA_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final RegObject<Block> BRIMSTONE_SLAG = RegisterBlocks.block("brimstone_slag", () -> new BrimstoneSlagBlock(slag()));
    public static final RegObject<Block> BRIMSTONE_SLAG_STAIRS = RegisterBlocks.block("brimstone_slag_stairs", () -> new StairBlock(BRIMSTONE_SLAG.get().defaultBlockState(), slag()));
    public static final RegObject<Block> BRIMSTONE_SLAG_SLAB = RegisterBlocks.block("brimstone_slag_slab", () -> new SlabBlock(slag()));
    public static final RegObject<Block> BRIMSTONE_SLAG_WALL = RegisterBlocks.block("brimstone_slag_wall", () -> new WallBlock(slag()));

    public static final RegObject<Block> BRIMSTONE_BRICKS = RegisterBlocks.block("brimstone_bricks", () -> new Block(slag()));
    public static final RegObject<Block> BRIMSTONE_BRICKS_STAIRS = RegisterBlocks.block("brimstone_bricks_stairs", () -> new StairBlock(BRIMSTONE_BRICKS.get().defaultBlockState(), slag()));
    public static final RegObject<Block> BRIMSTONE_BRICKS_SLAB = RegisterBlocks.block("brimstone_bricks_slab", () -> new SlabBlock(slag()));
    public static final RegObject<Block> BRIMSTONE_BRICKS_WALL = RegisterBlocks.block("brimstone_bricks_wall", () -> new WallBlock(slag()));

    public static final RegObject<Block> SMOOTH_BRIMSTONE_SLAG = RegisterBlocks.block("smooth_brimstone_slag", () -> new Block(slag()));
    public static final RegObject<Block> SMOOTH_BRIMSTONE_SLAG_STAIRS = RegisterBlocks.block("smooth_brimstone_slag_stairs", () -> new StairBlock(SMOOTH_BRIMSTONE_SLAG.get().defaultBlockState(), slag()));
    public static final RegObject<Block> SMOOTH_BRIMSTONE_SLAG_SLAB = RegisterBlocks.block("smooth_brimstone_slag_slab", () -> new SlabBlock(slag()));
    public static final RegObject<Block> SMOOTH_BRIMSTONE_SLAG_WALL = RegisterBlocks.block("smooth_brimstone_slag_wall", () -> new WallBlock(slag()));

    private RegisterSlagBlocks() {}

    private static BlockBehaviour.Properties slag() {
        return BlockBehaviour.Properties.of()
            .strength(3.5f)
            .sound(SoundType.STONE)
            .mapColor(MapColor.WARPED_HYPHAE)
            .pushReaction(PushReaction.NORMAL)
            .requiresCorrectToolForDrops();
    }

    public static void init() {}
}
