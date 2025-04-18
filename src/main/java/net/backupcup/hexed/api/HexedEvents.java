package net.backupcup.hexed.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HexedEvents {
    public interface PostMine {
        public Event<PostMine> POST_MINE = EventFactory.createArrayBacked(PostMine.class, callbacks -> (stack, world, state, pos, player) -> {
            for (var callback : callbacks) {
                callback.postMine(stack, world, state, pos, player);
            }
        });

        void postMine(ItemStack stack, World world, BlockState state, BlockPos pos, PlayerEntity player);
    }
}

