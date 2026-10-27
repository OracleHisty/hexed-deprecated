package net.backupcup.hexed.block;

import net.backupcup.hexed.util.HexRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

/** A glowing stalk that grows upwards with bone meal. Brightest at the ends of its age range. */
public class PistilBlock extends Block implements BonemealableBlock {
    public static final int MAX_AGE = 25;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);
    /** 0 = base, 1 = middle, 2 = tip. */
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, 2);
    public static final ToIntFunction<BlockState> STATE_TO_LUMINANCE = state -> {
        int age = state.getValue(AGE);
        int half = MAX_AGE / 2;
        double distance = age <= half ? half - age : age - half;
        return (int) Math.ceil(distance / half * 15);
    };

    private static final VoxelShape SHAPE_FULL = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    private static final VoxelShape SHAPE_TOP = Block.box(6.0, 0.0, 6.0, 10.0, 11.0, 10.0);

    public PistilBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0).setValue(TYPE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, TYPE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(TYPE) != 2 ? SHAPE_FULL : SHAPE_TOP;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        int[] newState = getState(level, pos);
        level.setBlockAndUpdate(pos, state.setValue(TYPE, newState[0]).setValue(AGE, newState[1]));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        boolean onPistil = level.getBlockState(pos.below()).is(this);
        if (!canSupportCenter(level, pos.below(), Direction.UP) && !onPistil) {
            return Blocks.AIR.defaultBlockState();
        }
        int type = 0;
        if (onPistil) type = level.getBlockState(pos.above()).isAir() ? 2 : 1;
        return state.setValue(TYPE, type);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return canSupportCenter(level, pos.below(), Direction.UP) || (below.is(this) && below.getValue(AGE) < MAX_AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        if (state.getValue(AGE) == MAX_AGE) return false;
        for (int step = 0; step <= MAX_AGE - state.getValue(AGE); step++) {
            BlockState next = level.getBlockState(pos.above(step + 1));
            if (next.is(this)) continue;
            if (!next.canBeReplaced()) return false;
            return level.getBlockState(pos.above(step)).getValue(AGE) < MAX_AGE;
        }
        return false;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        for (int step = 0; step <= MAX_AGE - state.getValue(AGE); step++) {
            BlockPos target = pos.above(step + 1);
            if (level.getBlockState(target).is(this)) continue;
            if (!level.getBlockState(target).canBeReplaced() || level.getBlockState(pos.above(step)).getValue(AGE) >= MAX_AGE) return;

            int roll = HexRandom.nextInt(step + state.getValue(AGE), MAX_AGE + 2);
            if (roll + 1 == MAX_AGE) {
                level.setBlockAndUpdate(target, HexRandom.nextFloat() >= 0.5 ? Blocks.SHROOMLIGHT.defaultBlockState() : Blocks.GLOWSTONE.defaultBlockState());
            } else {
                int[] newState = getState(level, target);
                level.setBlockAndUpdate(target, state.setValue(TYPE, newState[0]).setValue(AGE, newState[1]));
            }
            return;
        }
    }

    /** Returns {type, age} for a pistil at {@code pos}: it continues the stalk below, or starts a new one. */
    private int[] getState(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(this)) {
            int type = level.getBlockState(pos.above()).isAir() ? 2 : 1;
            return new int[]{type, Math.min(MAX_AGE, below.getValue(AGE) + 1)};
        }
        return new int[]{0, HexRandom.nextInt(0, MAX_AGE)};
    }
}
