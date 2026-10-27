package net.backupcup.hexed.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

/** A two block tall candle. Both halves share the lit state. */
public abstract class AbstractTallCandle extends Block {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final ToIntFunction<BlockState> STATE_TO_LUMINANCE = state -> state.getValue(LIT) ? 15 : 0;

    private static final VoxelShape TOP_SHAPE = Block.box(6.0, -16.0, 6.0, 10.0, 10.5, 10.0);
    private static final VoxelShape BOTTOM_SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 26.5, 10.0);

    protected AbstractTallCandle(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false).setValue(TOP, false));
    }

    protected abstract ParticleOptions extinguishParticle();

    public static void setLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        level.setBlock(pos, state.setValue(LIT, lit), Block.UPDATE_ALL);
        if (state.getValue(TOP)) {
            level.setBlockAndUpdate(pos.below(), state.setValue(LIT, lit).setValue(TOP, false));
        } else {
            level.setBlockAndUpdate(pos.above(), state.setValue(LIT, lit).setValue(TOP, true));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, TOP);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(TOP) ? TOP_SHAPE : BOTTOM_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide() && projectile.isOnFire() && !state.getValue(LIT)) {
            setLit(level, hit.getBlockPos(), state, true);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getAbilities().mayBuild || !state.getValue(LIT)) return InteractionResult.PASS;

        setLit(level, pos, state, false);
        player.playSound(SoundEvents.FIRE_EXTINGUISH, 1f, 1f);
        level.addParticle(extinguishParticle(), pos.getX() + .5, pos.getY() + 1.0, pos.getZ() + .5, 0.0, 0.0625, 0.0);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.DEBUG_STICK)) return ItemInteractionResult.CONSUME;
        if (!player.getAbilities().mayBuild) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        boolean flintAndSteel = stack.getItem() instanceof FlintAndSteelItem;
        if ((flintAndSteel || stack.is(Items.FIRE_CHARGE)) && !state.getValue(LIT)) {
            setLit(level, pos, state, true);
            player.playSound(SoundEvents.FIRECHARGE_USE, 1f, 1f);
            if (!player.isCreative()) {
                if (flintAndSteel) {
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                } else {
                    stack.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isInWorldBounds(pos.above())) {
            level.setBlockAndUpdate(pos.above(), defaultBlockState().setValue(TOP, true));
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!newState.is(this)) {
            breakPieces(level, pos);
        } else if (newState.getValue(LIT) != state.getValue(LIT)) {
            setLit(level, pos, state, newState.getValue(LIT));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos().above()).canBeReplaced(context)) {
            return defaultBlockState().setValue(TOP, false);
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    private void breakPieces(LevelAccessor level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        if (above.is(this) && above.getValue(TOP)) {
            level.destroyBlock(pos.above(), false);
        }
        BlockState below = level.getBlockState(pos.below());
        if (below.is(this) && !below.getValue(TOP)) {
            level.destroyBlock(pos.below(), false);
        }
    }
}
