package net.backupcup.hexed.item;

import net.backupcup.hexed.block.BrimstoneFireBlock;
import net.backupcup.hexed.register.RegisterBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/** Flint and Steel that lights Brimstone Fire on any block with a solid top, not only on brimstone. */
public class BrimstoneFlintAndSteelItem extends FlintAndSteelItem {
    public BrimstoneFlintAndSteelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState clicked = level.getBlockState(context.getClickedPos());
        // Campfires and candles light the same way as with normal Flint and Steel.
        if (CampfireBlock.canLight(clicked) || CandleBlock.canLight(clicked) || CandleCakeBlock.canLight(clicked)) {
            return super.useOn(context);
        }

        BlockPos firePos = context.getClickedPos().relative(context.getClickedFace());
        BlockState fire = RegisterBlocks.BRIMSTONE_FIRE.get().defaultBlockState().setValue(BrimstoneFireBlock.ANCHORED, true);
        if (!level.getBlockState(firePos).isAir() || !fire.canSurvive(level, firePos)) {
            return InteractionResult.FAIL;
        }

        Player player = context.getPlayer();
        level.playSound(player, firePos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, level.getRandom().nextFloat() * 0.4f + 0.8f);
        level.setBlock(firePos, fire, Block.UPDATE_ALL_IMMEDIATE);
        level.gameEvent(player, GameEvent.BLOCK_PLACE, context.getClickedPos());

        ItemStack stack = context.getItemInHand();
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, firePos, stack);
        }
        if (player != null) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
