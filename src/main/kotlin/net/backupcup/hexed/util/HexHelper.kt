package net.backupcup.hexed.util

import net.backupcup.hexed.Hexed
import net.backupcup.hexed.altar.AccursedAltarScreenHandler
import net.backupcup.hexed.enchantments.AbstractHex
import net.backupcup.hexed.register.RegisterEnchantments
import net.backupcup.hexed.register.RegisterTags.CALAMITOUS_ARMOR
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerListener
import net.minecraft.text.MutableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

object  HexHelper {
    private val blockedHexList by lazy { generateHexAvailability() }

    private fun generateHexAvailability(): List<AbstractHex> = Hexed.getConfig()?.let { config ->
        buildList {
            if (!config.aflameHex.shouldRegister) add(RegisterEnchantments.AFLAME_HEX)
            if (config.persecutedHex.shouldRegister) add(RegisterEnchantments.PERSECUTED_HEX)
            if (config.ephemeralHex.shouldRegister) add(RegisterEnchantments.EPHEMERAL_HEX)
            if (config.vindictiveHex.shouldRegister) add(RegisterEnchantments.VINDICTIVE_HEX)
            if (config.traitorousHex.shouldRegister) add(RegisterEnchantments.TRAITOROUS_HEX)
            if (config.displacedHex.shouldRegister) add(RegisterEnchantments.DISPLACED_HEX)
            if (config.avertingHex.shouldRegister) add(RegisterEnchantments.AVERTING_HEX)
            if (config.aquatiqueHex.shouldRegister) add(RegisterEnchantments.AQUATIQUE_HEX)
            if (config.dynamiqueHex.shouldRegister) add(RegisterEnchantments.DYNAMIQUE_HEX)
            if (config.ironcladHex.shouldRegister) add(RegisterEnchantments.IRONCLAD_HEX)
            if (config.franticHex.shouldRegister) add(RegisterEnchantments.FRANTIC_HEX)
            if (config.bloodthirstyHex.shouldRegister) add(RegisterEnchantments.BLOODTHIRSTY_HEX)
            if (config.disfigurementHex.shouldRegister) add(RegisterEnchantments.DISFIGUREMENT_HEX)
            if (config.metamorphosisHex.shouldRegister) add(RegisterEnchantments.METAMORPHOSIS_HEX)
            if (config.divineHex.shouldRegister) add(RegisterEnchantments.DIVINE_HEX)
            if (config.celebrationHex.shouldRegister) add(RegisterEnchantments.CELEBRATION_HEX)
            if (config.flaringHex.shouldRegister) add(RegisterEnchantments.FLARING_HEX)
            if (config.lingerHex.shouldRegister) add(RegisterEnchantments.LINGER_HEX)
            if (config.seizeHex.shouldRegister) add(RegisterEnchantments.SEIZE_HEX)
            if (config.sepultureHex.shouldRegister) add(RegisterEnchantments.SEPULTURE_HEX)
            if (config.ruinousHex.shouldRegister) add(RegisterEnchantments.RUINOUS_HEX)
            if (config.amplifyHex.shouldRegister) add(RegisterEnchantments.AMPLIFY_HEX)
            if (config.overburdenHex.shouldRegister) add(RegisterEnchantments.OVERBURDEN_HEX)
            if (config.famishmentHex.shouldRegister) add(RegisterEnchantments.FAMISHMENT_HEX)
            if (config.aggravateHex.shouldRegister) add(RegisterEnchantments.AGGRAVATE_HEX)
            if (config.volatilityHex.shouldRegister) add(RegisterEnchantments.VOLATILITY_HEX)
            if (config.phasedHex.shouldRegister) add(RegisterEnchantments.PHASED_HEX)
            if (config.overclockHex.shouldRegister) add(RegisterEnchantments.OVERCLOCK_HEX)
            if (config.provisionHex.shouldRegister) add(RegisterEnchantments.PROVISION_HEX)
            if (config.resentfulHex.shouldRegister) add(RegisterEnchantments.RESENTFUL_HEX)
        }
    } ?: emptyList()

    fun generatorListener(context: ScreenHandlerContext, player: PlayerEntity): ScreenHandlerListener {
        return object : ScreenHandlerListener {
            override fun onPropertyUpdate(handler: ScreenHandler, property: Int, value: Int) {}
            override fun onSlotUpdate(handler: ScreenHandler, slotId: Int, stack: ItemStack) {
                val item = handler.getSlot(1).stack
                if (!item.isEmpty) {
                    context.run { world, _ ->
                        val currentHandler = world.server?.playerManager?.getPlayer(player.uuid)?.currentScreenHandler
                        (currentHandler as AccursedAltarScreenHandler).availableHexList = (getAvailableHexList(item))
                        currentHandler.currentHex = getAvailableHexList(item)[0]
                    }
                }
            }
        }
    }

    fun getEnchantments(itemStack: ItemStack): List<Enchantment> {
        return EnchantmentHelper.get(itemStack).map { (enchantment, _) -> enchantment}
    }

    private fun getHexList(itemStack: ItemStack): List<AbstractHex> {
        return Registries.ENCHANTMENT.filterIsInstance<AbstractHex>().filter { hex ->
            hex.isAcceptableItem(itemStack) && !blockedHexList.contains(hex)}
    }

    fun getAvailableHexList(itemStack: ItemStack): List<AbstractHex> {
        return getHexList(itemStack).filterNot { getEnchantments(itemStack).contains(it) }
    }

    fun stackHasEnchantment(stack: ItemStack, key: Enchantment): Boolean {
        return EnchantmentHelper.get(stack).containsKey(key)
    }

    fun stackHasEnchantment(stackList: List<ItemStack>, key: Enchantment): Boolean {
        stackList.forEach { stack ->
            if (EnchantmentHelper.get(stack).containsKey(key)) return true
        }
        return false
    }

    fun stackHasEnchantment(stack: ItemStack, keyList: List<Enchantment>): Boolean {
        keyList.forEach { enchantment ->
            if (EnchantmentHelper.get(stack).containsKey(enchantment)) return true
        }
        return false
    }

    fun stackHasEnchantment(stackList: List<ItemStack>, keyList: List<Enchantment>): Boolean {
        stackList.forEach { stack ->
            keyList.forEach { key -> if (EnchantmentHelper.get(stack).containsKey(key)) return true }
        }
        return false
    }

    fun hasFullRobes(armorStack: Iterable<ItemStack>): Boolean {
        if (Hexed.getConfig()?.shouldArmorApply() == true) {
            for (piece in armorStack) {
                if (!piece.isIn(CALAMITOUS_ARMOR)) return false
            }
            return true
        }
        return false
    }

    fun hasFullRobes(entity: LivingEntity): Boolean {
        if (Hexed.getConfig()?.shouldArmorApply() == true) {
            entity.armorItems.forEach { piece ->
                if (!piece.isIn(CALAMITOUS_ARMOR)) return false
            }
            return true
        }
        return false
    }

    fun runeTexture(string: String): Identifier {
        return Identifier(Hexed.MOD_ID, "textures/gui/runes/$string.png")
    }

    fun entityMultiplyingEffect(user: LivingEntity, effect: StatusEffect, duration: Int, decayLength: Int) {
        if (user.hasStatusEffect(effect)) {
            val effectAmplifier = user.getStatusEffect(effect)?.amplifier?.plus(1)

            for (i in 0..effectAmplifier!!) {
                user.addStatusEffect(
                    StatusEffectInstance(
                        effect,
                        duration + (effectAmplifier - i) * decayLength, i,
                        true, false, true
                    )
                )
            }
        } else {
            user.addStatusEffect(
                StatusEffectInstance(
                    effect,
                    duration, 0,
                    true, false, true
                )
            )
        }
    }

    fun entityMultiplyingEffect(user: LivingEntity, effect: StatusEffect, duration: Int, decayLength: Int, ambient: Boolean, showParticles: Boolean, showIcon: Boolean) {
        if (user.hasStatusEffect(effect)) {
            val effectAmplifier = user.getStatusEffect(effect)?.amplifier?.plus(1)

            for (i in 0..effectAmplifier!!) {
                user.addStatusEffect(
                    StatusEffectInstance(
                        effect,
                        duration + (effectAmplifier - i) * decayLength, i,
                        ambient, showParticles, showIcon
                    )
                )
            }
        } else {
            user.addStatusEffect(
                StatusEffectInstance(
                    effect,
                    duration, 0,
                    ambient, showParticles, showIcon
                )
            )
        }
    }

    @JvmStatic
    fun modifyTooltip(text: MutableText, itemStack: ItemStack): MutableText {
        if (getEnchantments(itemStack).filterIsInstance<AbstractHex>().isNotEmpty() || itemStack.item is TaintedItem<*>) {
            return text.bold().darkRed()
        }
        return text
    }
}