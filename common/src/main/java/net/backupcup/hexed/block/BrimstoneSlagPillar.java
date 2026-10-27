package net.backupcup.hexed.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A pillar that connects to pillars above and below it. */
public class BrimstoneSlagPillar extends Block {
    /** 0 = alone, 1 = pillar below, 2 = pillar above, 3 = both. */
    public static final IntegerProperty STATE = IntegerProperty.create("state", 0, 3);

    private static final VoxelShape SHAPE_MIDDLE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
    private static final VoxelShape SHAPE_TOP = Shapes.or(SHAPE_MIDDLE, Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0));
    private static final VoxelShape SHAPE_BOTTOM = Shapes.or(SHAPE_MIDDLE, Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0));
    private static final VoxelShape SHAPE_BOTH = Shapes.or(SHAPE_TOP, SHAPE_BOTTOM);

    public BrimstoneSlagPillar(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STATE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(STATE)) {
            case 1 -> SHAPE_TOP;
            case 2 -> SHAPE_BOTTOM;
            case 3 -> SHAPE_MIDDLE;
            default -> SHAPE_BOTH;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    private int connections(BlockGetter level, BlockPos pos) {
        int state = 0;
        if (level.getBlockState(pos.below()).is(this)) state += 1;
        if (level.getBlockState(pos.above()).is(this)) state += 2;
        return state;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        level.setBlockAndUpdate(pos, state.setValue(STATE, connections(level, pos)));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        return state.setValue(STATE, connections(level, pos));
    }
}
