package net.backupcup.hexed.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.backupcup.hexed.block.BrimstoneFireBlock;
import net.backupcup.hexed.register.RegisterBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fire lit or spreading onto brimstone blocks becomes Brimstone Fire, the way it becomes Soul Fire on soul sand.
 * Covers Flint and Steel, fire charges and fire spread, which all pick the fire through this method.
 */
@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {
    @ModifyReturnValue(method = "getState", at = @At("RETURN"))
    private static BlockState hexed$brimstoneFire(BlockState original, BlockGetter level, BlockPos pos) {
        if (BrimstoneFireBlock.isBase(level, pos.below())) {
            return RegisterBlocks.BRIMSTONE_FIRE.get().defaultBlockState();
        }
        return original;
    }
}
