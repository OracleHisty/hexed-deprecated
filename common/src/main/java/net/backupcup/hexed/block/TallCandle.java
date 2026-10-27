package net.backupcup.hexed.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TallCandle extends AbstractTallCandle {
    public TallCandle(Properties properties) {
        super(properties);
    }

    @Override
    protected ParticleOptions extinguishParticle() {
        return ParticleTypes.CAMPFIRE_COSY_SMOKE;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hexed.abstract_candle.line_1").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT) || !state.getValue(TOP)) return;

        float roll = random.nextFloat();
        if (roll < .15f) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + .5, pos.getY() + 0.85, pos.getZ() + .5, 0.0, 0.0, 0.0);
            if (roll < .07f) {
                level.playLocalSound(pos.getX() + .5, pos.getY() + 0.85, pos.getZ() + .5,
                    SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS,
                    random.nextFloat() + 1f, random.nextFloat() * .7f + .3f, false);
            }
        }
        level.addParticle(ParticleTypes.FLAME, pos.getX() + .5, pos.getY() + 0.85, pos.getZ() + .5, 0.0, 0.0, 0.0);
    }
}
