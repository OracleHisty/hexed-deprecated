package net.backupcup.hexed.altar

import net.backupcup.hexed.altar.AccursedAltarBlockEntity.Companion.CANDLE_OFFSETS
import net.backupcup.hexed.block.AbstractTallCandle
import net.backupcup.hexed.block.lit
import net.backupcup.hexed.register.RegisterBlockEntities
import net.backupcup.hexed.register.RegisterBlocks
import net.backupcup.hexed.register.RegisterSounds
import net.backupcup.hexed.util.*
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.particle.DustParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class AccursedAltarBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(RegisterBlockEntities.ACCURSED_ALTAR_BLOCK_ENTITY, pos, state), NamedScreenHandlerFactory {
    private var isActive = validBlockState?.let { it.block == RegisterBlocks.ACCURSED_ALTAR && it.active } ?: false

    fun getActiveState(): Boolean {
        markDirty()
        return validBlockState?.let { it.block == RegisterBlocks.ACCURSED_ALTAR && it.lit } ?: false
    }

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return AccursedAltarScreenHandler(syncId, playerInventory, player, ScreenHandlerContext.EMPTY, this)
    }

    override fun getDisplayName(): Text = cachedState.block.translationKey.translate()

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        isActive = nbt.getBoolean("active")
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.putBoolean("active", isActive == true)
    }

    override fun toInitialChunkDataNbt(): NbtCompound = createNbt()

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    companion object {
        val CANDLE_OFFSETS = listOf(
            listOf(
                BlockPos(-2, 1, -3), BlockPos(-2, 1, 3),
                BlockPos(2, 1, -3), BlockPos(2, 1, 3),
                BlockPos(3, 2, 0), BlockPos(-3, 2, 0)
            ),
            listOf(
                BlockPos(-3, 1, -2), BlockPos(-3, 1, 2),
                BlockPos(3, 1, -2), BlockPos(3, 1, 2),
                BlockPos(0, 2, 3), BlockPos(0, 2, -3)
            )
        )

        fun clientTick(world: World, pos: BlockPos, state: BlockState, blockEntity: AccursedAltarBlockEntity) {
            if (!state.active) {
                if (state.get(AccursedAltar.FACING).axis == Direction.Axis.Z) {
                    CANDLE_OFFSETS[0].forEach {particlePos ->
                        blockEntity.clientCheckCandles(blockEntity, particlePos, world, pos)
                    }
                } else {
                    CANDLE_OFFSETS[1].forEach {particlePos ->
                        blockEntity.clientCheckCandles(blockEntity, particlePos, world, pos)
                    }
                }
                if (world.time % 20L == 0L) {
                    world.addParticle(
                        ParticleTypes.ANGRY_VILLAGER,
                        pos.x + 0.5,
                        pos.y + 1.0,
                        pos.z + 0.5,
                        HexRandom.nextDouble(-.025, .025), HexRandom.nextDouble(-.025, .025), HexRandom.nextDouble(-.025, .025)
                    )}
            } else {
                world.addParticle(
                    DustParticleEffect.DEFAULT,
                    pos.x + 0.5,
                    pos.y + 1.0,
                    pos.z + 0.5,
                    HexRandom.nextDouble(-.025, .025), HexRandom.nextDouble(-.025, .025), HexRandom.nextDouble(-.025, .025)
                )
            }
        }



        fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: AccursedAltarBlockEntity) {
            if (world.time % 20L == 0L) {

                val isZ = state.facing.axis == Direction.Axis.Z
                val isActivate = !state.active
                val offset = if(isZ) 0 else 1
                val operation: (Int) -> Boolean = if(isActivate) { a -> a == 6 } else { a -> a < 6 }

                if (blockEntity.serverCheckCandles(CANDLE_OFFSETS[offset], world, pos, blockEntity).let(operation)) blockEntity.altarStateSet(world, pos, state, isActivate)
            }
        }
    }

    private fun posGetCandle(world: World, pos: BlockPos, offset: BlockPos): Boolean = world.getBlockState(pos.add(offset)).block == RegisterBlocks.BRIMSTONE_CANDLE

    private fun posGetCandleLit(world: World, pos: BlockPos, offset: BlockPos): Boolean = world.getBlockState(pos.add(offset)).lit

    private fun altarStateSet(world: World, pos: BlockPos, state: BlockState, newState: Boolean) {
        world.playSound(
            null, pos,
            if(newState) RegisterSounds.ACCURSED_ALTAR_ACTIVATE else SoundEvents.BLOCK_FIRE_EXTINGUISH,
            SoundCategory.BLOCKS,
            0.25f, 1f)

        world.setBlockState(pos, state.with(AccursedAltar.ACTIVE, newState))
    }

    var lit: Boolean
        get() = false
        set(newState) {
            val blockPos: BlockPos = this.pos
            val world: World = this.world ?: return
            val offsetList: List<BlockPos> =
                if (world.getBlockState(blockPos)?.get(AccursedAltar.FACING)?.axis == Direction.Axis.Z)
                    CANDLE_OFFSETS[0]
                else
                    CANDLE_OFFSETS[1]

            offsetList.map { blockPos + it }.forEach { candlePos ->
                val state = world.getBlockState(candlePos)
                if (state.isOf(RegisterBlocks.BRIMSTONE_CANDLE)) {
                    if (state.lit) {
                        AbstractTallCandle.setLit(world, candlePos, state, newState)
                        world.playBlockSound(candlePos, SoundEvents.BLOCK_FIRE_EXTINGUISH, 0.125f, 1f)
                    }
                }
            }
        }

    private fun serverCheckCandles(offsetList: List<BlockPos>, world: World, pos: BlockPos, blockEntity: AccursedAltarBlockEntity): Int {
        var candlesMatch = 0

        for (candleOffset in offsetList) {
            if (!blockEntity.posGetCandle(world, pos, candleOffset)) continue
            if (!blockEntity.posGetCandleLit(world, pos, candleOffset)) continue
            candlesMatch++
        }
        return candlesMatch
    }

    fun clientCheckCandles(blockEntity: AccursedAltarBlockEntity, particlePos: BlockPos, world: World, pos: BlockPos) {
        if (!blockEntity.posGetCandle(world, pos, particlePos)) {
            world.addParticle(
                DustParticleEffect.DEFAULT,
                pos.x + particlePos.x.toDouble() + 0.5,
                pos.y + particlePos.y.toDouble() + 0.5,
                pos.z + particlePos.z.toDouble() + 0.5,
                HexRandom.nextDouble(-.025, .025), HexRandom.nextDouble(-.025, .025), HexRandom.nextDouble(-.025, .025)
            )
        } else {
            if(!blockEntity.posGetCandleLit(world, pos, particlePos)) {
                world.addParticle(
                    DustParticleEffect.DEFAULT,
                    pos.x + particlePos.x.toDouble() + 0.5,
                    pos.y + particlePos.y.toDouble() + 2.0,
                    pos.z + particlePos.z.toDouble() + 0.5,
                    HexRandom.nextDouble(-.025, .025), HexRandom.nextDouble(-.025, .025), HexRandom.nextDouble(-.025, .025)
                )
            }
        }
    }
}