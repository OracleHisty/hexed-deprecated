package net.backupcup.hexed.block;

import com.mojang.serialization.MapCodec;
import net.backupcup.hexed.register.RegisterSlagBlocks;
import net.backupcup.hexed.util.HexRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.NyliumBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

/** Nylium-like slag that grows Lavendin Verdure and Lava Pistils. */
public class LavendinCinderBlock extends NyliumBlock {
    public static final MapCodec<LavendinCinderBlock> CODEC = simpleCodec(LavendinCinderBlock::new);

    public LavendinCinderBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<NyliumBlock> codec() {
        @SuppressWarnings("unchecked")
        MapCodec<NyliumBlock> codec = (MapCodec<NyliumBlock>) (MapCodec<?>) CODEC;
        return codec;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (random.nextFloat() >= 0.5f) {
            level.setBlockAndUpdate(pos.above(), RegisterSlagBlocks.LAVENDIN_VERDURE.get().defaultBlockState());
        } else {
            level.setBlockAndUpdate(pos.above(), RegisterSlagBlocks.LAVA_PISTIL.get().defaultBlockState()
                .setValue(PistilBlock.AGE, HexRandom.nextInt(0, PistilBlock.MAX_AGE)));
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!stayAlive(state, level, pos)) {
            level.setBlockAndUpdate(pos, RegisterSlagBlocks.BRIMSTONE_SLAG.get().defaultBlockState());
        }
    }

    private static boolean stayAlive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        int opacity = LightEngine.getLightBlockInto(level, state, pos, aboveState, above, Direction.UP, aboveState.getLightBlock(level, above));
        return opacity < level.getMaxLightLevel();
    }
}
