package net.backupcup.hexed.util

import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry
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
        val entry: RegistryEntry<T> = registry.getEntry(value) ?: return false
        return entry.isIn(tag)
    }
}