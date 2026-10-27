package net.backupcup.hexed.altar;

import net.backupcup.hexed.block.AbstractTallCandle;
import net.backupcup.hexed.register.RegisterBlockEntities;
import net.backupcup.hexed.register.RegisterBlocks;
import net.backupcup.hexed.register.RegisterSounds;
import net.backupcup.hexed.util.HexRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AccursedAltarBlockEntity extends BlockEntity implements MenuProvider {
    /** Where the six Brimstone Candles have to stand, for altars facing along Z and along X. */
    public static final List<List<BlockPos>> CANDLE_OFFSETS = List.of(
        List.of(
            new BlockPos(-2, 1, -3), new BlockPos(-2, 1, 3),
            new BlockPos(2, 1, -3), new BlockPos(2, 1, 3),
            new BlockPos(3, 2, 0), new BlockPos(-3, 2, 0)
        ),
        List.of(
            new BlockPos(-3, 1, -2), new BlockPos(-3, 1, 2),
            new BlockPos(3, 1, -2), new BlockPos(3, 1, 2),
            new BlockPos(0, 2, 3), new BlockPos(0, 2, -3)
        )
    );

    private boolean active;

    public AccursedAltarBlockEntity(BlockPos pos, BlockState state) {
        super(RegisterBlockEntities.ACCURSED_ALTAR.get(), pos, state);
        this.active = state.getValue(AccursedAltarBlock.ACTIVE);
    }

    public boolean isActive() {
        BlockState state = getBlockState();
        return state.is(RegisterBlocks.ACCURSED_ALTAR.get()) && state.getValue(AccursedAltarBlock.ACTIVE);
    }

    private static List<BlockPos> candleOffsets(BlockState state) {
        return CANDLE_OFFSETS.get(state.getValue(AccursedAltarBlock.FACING).getAxis() == Direction.Axis.Z ? 0 : 1);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AccursedAltarMenu(containerId, inventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        active = tag.getBoolean("active");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("active", active);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AccursedAltarBlockEntity altar) {
        if (level.getGameTime() % 20L != 0L) return;

        boolean activate = !state.getValue(AccursedAltarBlock.ACTIVE);
        int litCandles = countLitCandles(level, pos, state);
        if (activate ? litCandles == 6 : litCandles < 6) {
            setActive(level, pos, state, activate);
            altar.active = activate;
            altar.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AccursedAltarBlockEntity altar) {
        if (state.getValue(AccursedAltarBlock.ACTIVE)) {
            level.addParticle(DustParticleOptions.REDSTONE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                jitter(), jitter(), jitter());
            return;
        }

        // Point out the missing or unlit candles.
        for (BlockPos offset : candleOffsets(state)) {
            BlockState candle = level.getBlockState(pos.offset(offset));
            if (!candle.is(RegisterBlocks.BRIMSTONE_CANDLE.get())) {
                level.addParticle(DustParticleOptions.REDSTONE,
                    pos.getX() + offset.getX() + 0.5, pos.getY() + offset.getY() + 0.5, pos.getZ() + offset.getZ() + 0.5,
                    jitter(), jitter(), jitter());
            } else if (!candle.getValue(AbstractTallCandle.LIT)) {
                level.addParticle(DustParticleOptions.REDSTONE,
                    pos.getX() + offset.getX() + 0.5, pos.getY() + offset.getY() + 2.0, pos.getZ() + offset.getZ() + 0.5,
                    jitter(), jitter(), jitter());
            }
        }

        if (level.getGameTime() % 20L == 0L) {
            level.addParticle(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                jitter(), jitter(), jitter());
        }
    }

    private static double jitter() {
        return HexRandom.nextDouble(-.025, .025);
    }

    private static int countLitCandles(Level level, BlockPos pos, BlockState state) {
        int lit = 0;
        for (BlockPos offset : candleOffsets(state)) {
            BlockState candle = level.getBlockState(pos.offset(offset));
            if (candle.is(RegisterBlocks.BRIMSTONE_CANDLE.get()) && candle.getValue(AbstractTallCandle.LIT)) lit++;
        }
        return lit;
    }

    private static void setActive(Level level, BlockPos pos, BlockState state, boolean active) {
        level.playSound(null, pos,
            active ? RegisterSounds.ACCURSED_ALTAR_ACTIVATE.get() : SoundEvents.FIRE_EXTINGUISH,
            SoundSource.BLOCKS, 0.25f, 1f);
        level.setBlockAndUpdate(pos, state.setValue(AccursedAltarBlock.ACTIVE, active));
    }

    /** Puts out all of the altar's candles, which happens every time it hexes an item. */
    public void extinguishCandles() {
        Level level = getLevel();
        if (level == null) return;

        for (BlockPos offset : candleOffsets(getBlockState())) {
            BlockPos candlePos = getBlockPos().offset(offset);
            BlockState candle = level.getBlockState(candlePos);
            if (candle.is(RegisterBlocks.BRIMSTONE_CANDLE.get()) && candle.getValue(AbstractTallCandle.LIT)) {
                AbstractTallCandle.setLit(level, candlePos, candle, false);
                level.playSound(null, candlePos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.125f, 1f);
            }
        }
    }
}
