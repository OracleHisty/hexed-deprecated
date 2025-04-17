package net.backupcup.hexed.util

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun PlayerEntity.sendTranslated(message: String) = this.sendMessage(Text.translatable(message))

fun PlayerEntity.sendMessage(overlay: Boolean = false, block: MutableText.() -> Unit) {
    val text = Text.empty();
    block(text)
    this.sendMessage(text, overlay)
}

fun String.translate(): MutableText = Text.translatable(this)

fun MutableText.translate(message: String) = this.append(message.translate())
fun MutableText.red() = this.formatted(Formatting.RED)
fun MutableText.yellow() = this.formatted(Formatting.YELLOW)
fun MutableText.blue() = this.formatted(Formatting.BLUE)
fun MutableText.bold() = this.formatted(Formatting.BOLD)
fun MutableText.darkRed() = this.formatted(Formatting.DARK_RED)
fun MutableText.gray() = this.formatted(Formatting.GRAY)
fun MutableText.italic() = this.formatted(Formatting.ITALIC)

operator fun BlockPos.plus(offset: BlockPos): BlockPos = this.add(offset)

fun World.playBlockSound(pos: BlockPos, sound: SoundEvent, volume: Float, pitch: Float) = this.playSound(null, pos, sound, SoundCategory.BLOCKS, volume, pitch)

val BlockEntity.validBlockState: BlockState?
    get() = this.world?.getBlockState(this.pos)