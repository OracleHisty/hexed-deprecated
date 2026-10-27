package net.backupcup.hexed.altar;

import com.mojang.serialization.MapCodec;
import net.backupcup.hexed.register.RegisterBlockEntities;
import net.backupcup.hexed.register.RegisterStats;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** The Accursed Altar. It only opens while its six Brimstone Candles are placed and lit. */
public class AccursedAltarBlock extends BaseEntityBlock {
    public static final MapCodec<AccursedAltarBlock> CODEC = simpleCodec(AccursedAltarBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(0.0, 5.0, 0.0, 3.0, 16.0, 3.0),
        Block.box(13.0, 5.0, 0.0, 16.0, 16.0, 3.0),
        Block.box(2.0, 0.0, 2.0, 14.0, 8.0, 14.0),
        Block.box(1.0, 8.0, 1.0, 15.0, 12.0, 15.0),
        Block.box(13.0, 5.0, 13.0, 16.0, 16.0, 16.0),
        Block.box(0.0, 5.0, 13.0, 3.0, 16.0, 16.0)
    );

    public AccursedAltarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        for (int i = 1; i <= 4; i++) {
            Component line = Component.translatable("tooltip.hexed.accursed_altar.line_" + i);
            tooltip.add(i < 3
                ? line.copy().withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED, ChatFormatting.ITALIC)
                : line.copy().withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AccursedAltarBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (!state.getValue(ACTIVE)) {
                player.displayClientMessage(Component.translatable("message.hexed.altar_no_candles")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD, ChatFormatting.ITALIC), true);
                return InteractionResult.SUCCESS;
            }
            MenuProvider provider = state.getMenuProvider(level, pos);
            if (provider != null) {
                player.openMenu(provider);
                player.awardStat(RegisterStats.ACCURSED_ALTAR_OPENED.get());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RegisterBlockEntities.ACCURSED_ALTAR.get(), level.isClientSide()
            ? AccursedAltarBlockEntity::clientTick
            : AccursedAltarBlockEntity::serverTick);
    }
}
