package net.backupcup.hexed.block;

import net.backupcup.hexed.register.RegisterSlagBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/** A lava-like fluid. Lava behavior comes from the {@code minecraft:lava} fluid tag. */
public abstract class BlazingMagmaFluid extends FlowingFluid {
    @Override
    public Item getBucket() {
        return RegisterSlagBlocks.BLAZING_MAGMA_BUCKET.get();
    }

    @Override
    public Fluid getFlowing() {
        return RegisterSlagBlocks.FLOWING_BLAZING_MAGMA.get();
    }

    @Override
    public Fluid getSource() {
        return RegisterSlagBlocks.BLAZING_MAGMA_SOURCE.get();
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == getSource() || fluid == getFlowing();
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return level.dimensionType().ultraWarm() ? 15 : 7;
    }

    @Override
    protected float getExplosionResistance() {
        return 100f;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return RegisterSlagBlocks.BLAZING_MAGMA.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return level.dimensionType().ultraWarm() ? 6 : 3;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return level.dimensionType().ultraWarm() ? 1 : 2;
    }

    public static class Flowing extends BlazingMagmaFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends BlazingMagmaFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
