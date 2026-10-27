package net.backupcup.hexed.block;

import net.backupcup.hexed.register.RegisterSlagBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Bone meal turns slag into Lavendin Cinder. */
public class BrimstoneSlagBlock extends Block implements BonemealableBlock {
    public BrimstoneSlagBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        // TODO: only spread from nearby cinder once the biome is added
        return level.getBlockState(pos.above()).propagatesSkylightDown(level, pos);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, RegisterSlagBlocks.LAVENDIN_CINDER.get().defaultBlockState(), Block.UPDATE_ALL);
    }
}
