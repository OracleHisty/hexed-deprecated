package net.backupcup.hexed.block

import net.backupcup.hexed.register.RegisterSlagBlocks
import net.backupcup.hexed.util.playBlockSound
import net.minecraft.block.BlockState
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.Item
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView


abstract class BlazingMagmaFluid: FlowableFluid() {
    override fun getBucketItem(): Item = RegisterSlagBlocks.BLAZING_MAGMA_BUCKET

    override fun getFlowing(): Fluid = RegisterSlagBlocks.FLOW_BLAZING_MAGMA

    override fun getStill(): Fluid = RegisterSlagBlocks.STILL_BLAZING_MAGMA

    override fun matchesType(fluid: Fluid): Boolean = fluid === still || fluid === flowing

    override fun canBeReplacedWith(state: FluidState, world: BlockView, pos: BlockPos, fluid: Fluid, direction: Direction): Boolean = false

    override fun getTickRate(world: WorldView): Int = if (world.dimension.ultrawarm) 15 else 7

    override fun getBlastResistance(): Float = 100f

    override fun toBlockState(state: FluidState): BlockState = RegisterSlagBlocks.BLAZING_MAGMA.defaultState.with(Properties.LEVEL_15, getBlockStateLevel(state))

    override fun isStill(state: FluidState?): Boolean = false

    override fun isInfinite(world: World?): Boolean = false

    override fun beforeBreakingBlock(world: WorldAccess, pos: BlockPos, state: BlockState) =
        world.playBlockSound(pos, SoundEvents.BLOCK_LAVA_EXTINGUISH)

    override fun getFlowSpeed(world: WorldView): Int = if (world.dimension.ultrawarm) 6 else 3

    override fun getLevelDecreasePerBlock(world: WorldView): Int = if (world.dimension.ultrawarm) 1 else 2


    class Flowing: BlazingMagmaFluid() {
        override fun appendProperties(builder: StateManager.Builder<Fluid, FluidState>) {
            super.appendProperties(builder)
            builder.add(LEVEL)
        }

        override fun getLevel(fluidState: FluidState): Int = fluidState.get(LEVEL)

        override fun isStill(state: FluidState?): Boolean = false
    }


    class Still: BlazingMagmaFluid() {
        override fun getLevel(state: FluidState?): Int = 8
        override fun isStill(state: FluidState?): Boolean = true
    }

}