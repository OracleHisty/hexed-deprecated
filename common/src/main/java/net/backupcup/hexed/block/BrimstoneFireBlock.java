package net.backupcup.hexed.block;

import com.mojang.serialization.MapCodec;
import net.backupcup.hexed.register.RegisterTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Brimstone's answer to Soul Fire: lighting the top of a brimstone block ({@code #hexed:brimstone_fire_base_blocks})
 * makes this fire instead of normal fire. Like Soul Fire it doesn't spread or burn out.
 */
public class BrimstoneFireBlock extends BaseFireBlock {
    public static final MapCodec<BrimstoneFireBlock> CODEC = simpleCodec(BrimstoneFireBlock::new);

    /** Lit with Brimstone Flint and Steel: survives on any block with a solid top, not only brimstone. */
    public static final BooleanProperty ANCHORED = BooleanProperty.create("anchored");

    public BrimstoneFireBlock(Properties properties) {
        super(properties, 1.0f);
        registerDefaultState(stateDefinition.any().setValue(ANCHORED, false));
    }

    @Override
    protected MapCodec<? extends BaseFireBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ANCHORED);
    }

    /** Whether lighting a fire on top of {@code base} makes Brimstone Fire. */
    public static boolean isBase(BlockGetter level, BlockPos basePos) {
        BlockState base = level.getBlockState(basePos);
        return base.is(RegisterTags.BRIMSTONE_FIRE_BASE_BLOCKS) && base.isFaceSturdy(level, basePos, Direction.UP);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        if (state.getValue(ANCHORED)) {
            return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
        }
        return isBase(level, below);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected boolean canBurn(BlockState state) {
        return true;
    }
}
