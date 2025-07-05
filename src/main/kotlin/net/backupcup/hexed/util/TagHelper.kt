package net.backupcup.hexed.util

import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

object TagHelper {
    @JvmStatic
    fun <T> isIdentifierInTag(
        registry: Registry<T>,
        tag: TagKey<T>,
        id: Identifier
    ): Boolean {
        val value = registry.getOrEmpty(id).orElse(null) ?: return false
        return isIdentifierInTag(registry, tag, value)
    }

    @JvmStatic
    fun <T> isIdentifierInTag(
        registry: Registry<T>,
        tag: TagKey<T>,
        value: T
    ): Boolean {
        return registry.isIn(tag, value)
    }
    fun Enchantment.isIn(tagKey: TagKey<Enchantment>) = Registries.ENCHANTMENT.isIn(tagKey, this)
    
    fun <T> Registry<T>.isIn(tagKey: TagKey<T>, value: T): Boolean = getEntry(value) ?.isIn(tagKey)?: false
}