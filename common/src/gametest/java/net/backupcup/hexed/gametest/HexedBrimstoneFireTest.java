package net.backupcup.hexed.gametest;

import it.crystalnest.prometheus.api.type.FireTyped;
import net.backupcup.hexed.block.BrimstoneFireBlock;
import net.backupcup.hexed.flame.HexedFlames;
import net.backupcup.hexed.register.RegisterBlocks;
import net.backupcup.hexed.register.RegisterItems;
import net.backupcup.hexed.register.RegisterSlagBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Brimstone Fire placement rules and Brimstone Flint and Steel. */
public final class HexedBrimstoneFireTest {
    private HexedBrimstoneFireTest() {}

    public static void run(GameTestHelper helper) {
        BlockPos base = new BlockPos(0, 1, 0);
        BlockPos fire = base.above();

        // Fire lit on brimstone becomes Brimstone Fire; on stone it stays normal fire.
        helper.setBlock(base, RegisterSlagBlocks.BRIMSTONE_BRICKS.get());
        helper.assertTrue(BaseFireBlock.getState(helper.getLevel(), helper.absolutePos(fire)).is(RegisterBlocks.BRIMSTONE_FIRE.get()),
            "Fire on brimstone should be Brimstone Fire");
        helper.setBlock(base, Blocks.STONE);
        helper.assertTrue(BaseFireBlock.getState(helper.getLevel(), helper.absolutePos(fire)).is(Blocks.FIRE),
            "Fire on stone should stay normal fire");

        // Normal Flint and Steel on brimstone places it.
        helper.setBlock(base, RegisterSlagBlocks.SMOOTH_BRIMSTONE_SLAG.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        use(helper, player, new ItemStack(Items.FLINT_AND_STEEL), base);
        helper.assertBlockPresent(RegisterBlocks.BRIMSTONE_FIRE.get(), fire);

        // Unanchored Brimstone Fire goes out once its base isn't brimstone.
        helper.setBlock(base, Blocks.STONE);
        helper.assertBlockNotPresent(RegisterBlocks.BRIMSTONE_FIRE.get(), fire);

        // The brimstone Flint and Steel lights anchored Brimstone Fire on any block, and it stays.
        ItemStack lighter = new ItemStack(RegisterItems.BRIMSTONE_FLINT_AND_STEEL.get());
        InteractionResult result = use(helper, player, lighter, base);
        helper.assertTrue(result.consumesAction(), "Brimstone Flint and Steel should light stone, got " + result);
        helper.assertBlockPresent(RegisterBlocks.BRIMSTONE_FIRE.get(), fire);
        helper.assertBlockProperty(fire, BrimstoneFireBlock.ANCHORED, true);
        helper.assertTrue(lighter.getDamageValue() == 1, "Brimstone Flint and Steel should lose durability");
        helper.setBlock(base, Blocks.DIRT);
        helper.assertBlockPresent(RegisterBlocks.BRIMSTONE_FIRE.get(), fire);

        if (HexedFlames.PROMETHEUS.equals(HexedFlames.activeMod())) {
            BlockState state = RegisterBlocks.BRIMSTONE_FIRE.get().defaultBlockState();
            helper.assertTrue(HexedFlames.Flame.BRIMSTONE.id().equals(((FireTyped) state.getBlock()).getFireType()),
                "Prometheus should give Brimstone Fire the hexed:brimstone fire type");
        }

        helper.succeed();
    }

    private static InteractionResult use(GameTestHelper helper, Player player, ItemStack stack, BlockPos target) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(target);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute).add(0, 0.5, 0), Direction.UP, absolute, false);
        return stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }
}
