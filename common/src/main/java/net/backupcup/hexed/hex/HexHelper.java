package net.backupcup.hexed.hex;

import net.backupcup.hexed.Config;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.register.RegisterTags;
import net.backupcup.hexed.util.TaintedItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class HexHelper {
    private HexHelper() {}

    public static ItemEnchantments enchantments(ItemStack stack) {
        return stack.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    public static boolean isHex(Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey().map(Hexes::isHex).orElse(false);
    }

    public static boolean has(ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) return false;
        for (Holder<Enchantment> enchantment : enchantments(stack).keySet()) {
            if (enchantment.is(key)) return true;
        }
        return false;
    }

    @SafeVarargs
    public static boolean hasAny(ItemStack stack, ResourceKey<Enchantment>... keys) {
        for (ResourceKey<Enchantment> key : keys) {
            if (has(stack, key)) return true;
        }
        return false;
    }

    @SafeVarargs
    public static boolean hasAny(Iterable<ItemStack> stacks, ResourceKey<Enchantment>... keys) {
        for (ItemStack stack : stacks) {
            if (hasAny(stack, keys)) return true;
        }
        return false;
    }

    public static boolean hasAnyHex(ItemStack stack) {
        for (Holder<Enchantment> enchantment : enchantments(stack).keySet()) {
            if (isHex(enchantment)) return true;
        }
        return false;
    }

    public static boolean hasNonHexEnchantments(ItemStack stack) {
        for (Holder<Enchantment> enchantment : enchantments(stack).keySet()) {
            if (!isHex(enchantment)) return true;
        }
        return false;
    }

    /** The level of any enchantment by key, without needing a registry lookup for its holder. */
    public static int level(ItemStack stack, ResourceKey<Enchantment> key) {
        ItemEnchantments enchantments = enchantments(stack);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            if (enchantment.is(key)) return enchantments.getLevel(enchantment);
        }
        return 0;
    }

    public static boolean hasFullRobes(LivingEntity entity) {
        return hasFullRobes(entity.getArmorSlots());
    }

    public static boolean hasFullRobes(Iterable<ItemStack> armor) {
        Config config = Hexed.getConfig();
        if (config == null || !config.shouldArmorApply()) return false;
        for (ItemStack piece : armor) {
            if (!piece.is(RegisterTags.CALAMITOUS_ARMOR)) return false;
        }
        return true;
    }

    public static Iterable<ItemStack> allEquipment(LivingEntity entity) {
        List<ItemStack> stacks = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty()) stacks.add(stack);
        }
        return stacks;
    }

    public static Optional<Holder.Reference<Enchantment>> lookup(HolderLookup.Provider registries, ResourceKey<Enchantment> key) {
        return registries.lookupOrThrow(Registries.ENCHANTMENT).get(key);
    }

    /** The hexes that can still be put on this item at the altar, in declaration order. */
    public static List<Holder.Reference<Enchantment>> availableHexes(Level level, ItemStack stack) {
        List<Holder.Reference<Enchantment>> available = new ArrayList<>();
        if (stack.isEmpty()) return available;
        for (ResourceKey<Enchantment> key : Hexes.ALL) {
            if (!Hexes.isEnabled(key) || has(stack, key)) continue;
            lookup(level.registryAccess(), key)
                .filter(holder -> holder.value().canEnchant(stack))
                .ifPresent(available::add);
        }
        return available;
    }

    public static void removeHexes(ItemStack stack) {
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.removeIf(HexHelper::isHex));
    }

    public static void multiplyingEffect(LivingEntity user, Holder<MobEffect> effect, int duration, int decayLength) {
        multiplyingEffect(user, effect, duration, decayLength, true, false, true);
    }

    /**
     * Adds a stack of the effect on top of any existing ones. Each lower level keeps running for
     * {@code decayLength} ticks longer than the one above it, so the stack decays one level at a time.
     */
    public static void multiplyingEffect(LivingEntity user, Holder<MobEffect> effect, int duration, int decayLength,
                                         boolean ambient, boolean showParticles, boolean showIcon) {
        @Nullable MobEffectInstance current = user.getEffect(effect);
        if (current != null) {
            int amplifier = current.getAmplifier() + 1;
            for (int i = 0; i <= amplifier; i++) {
                user.addEffect(new MobEffectInstance(effect, duration + (amplifier - i) * decayLength, i, ambient, showParticles, showIcon));
            }
        } else {
            user.addEffect(new MobEffectInstance(effect, duration, 0, ambient, showParticles, showIcon));
        }
    }

    public static MutableComponent modifyTooltip(MutableComponent text, ItemStack stack) {
        if (hasAnyHex(stack) || stack.getItem() instanceof TaintedItem) {
            return text.withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED);
        }
        return text;
    }
}
