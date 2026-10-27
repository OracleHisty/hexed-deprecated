package net.backupcup.hexed.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mixin(value = ItemStack.class, priority = 10)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();

    @Shadow public abstract boolean isDamageableItem();

    @Unique
    private ItemStack hexed$self() {
        return (ItemStack) (Object) this;
    }

    /** Hexed items show their name in bold dark red. */
    @ModifyVariable(method = "getTooltipLines", at = @At("STORE"), ordinal = 0)
    private MutableComponent hexed$modifyTooltipName(MutableComponent text) {
        return HexHelper.modifyTooltip(text, hexed$self());
    }

    /** Hexes alone don't count as enchantments: no glint, and the item can still be enchanted normally. */
    @ModifyReturnValue(method = "isEnchanted", at = @At("RETURN"))
    private boolean hexed$hexesAreNotEnchantments(boolean original) {
        return original && HexHelper.hasNonHexEnchantments(hexed$self());
    }

    @ModifyReturnValue(method = "isEnchantable", at = @At("RETURN"))
    private boolean hexed$enchantableWithHexes(boolean original) {
        if (original || !getItem().isEnchantable(hexed$self())) return original;
        return HexHelper.hasAnyHex(hexed$self()) && !HexHelper.hasNonHexEnchantments(hexed$self());
    }

    @ModifyArg(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers;forEach(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V"),
        index = 1)
    private BiConsumer<Holder<Attribute>, AttributeModifier> hexed$displacedGroup(BiConsumer<Holder<Attribute>, AttributeModifier> output) {
        return hexed$displaced(output);
    }

    @ModifyArg(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers;forEach(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"),
        index = 1)
    private BiConsumer<Holder<Attribute>, AttributeModifier> hexed$displacedSlot(BiConsumer<Holder<Attribute>, AttributeModifier> output) {
        return hexed$displaced(output);
    }

    /**
     * Displaced swaps the item's own armor and toughness values. The item's modifiers are passed through a filter
     * that remembers the two values and hands each one the other's amount.
     */
    @Unique
    private BiConsumer<Holder<Attribute>, AttributeModifier> hexed$displaced(BiConsumer<Holder<Attribute>, AttributeModifier> output) {
        if (!HexHelper.has(hexed$self(), Hexes.DISPLACED)) return output;

        List<AttributeModifier> armor = new ArrayList<>();
        List<AttributeModifier> toughness = new ArrayList<>();
        ItemAttributeModifiers modifiers = hexed$self().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (modifiers.modifiers().isEmpty()) modifiers = getItem().getDefaultAttributeModifiers();
        modifiers.modifiers().forEach(entry -> {
            if (entry.attribute().is(Attributes.ARMOR)) armor.add(entry.modifier());
            else if (entry.attribute().is(Attributes.ARMOR_TOUGHNESS)) toughness.add(entry.modifier());
        });
        if (armor.isEmpty()) return output;

        double armorAmount = armor.get(0).amount();
        double toughnessAmount = toughness.isEmpty() ? 0 : toughness.get(0).amount();
        return (attribute, modifier) -> {
            if (attribute.is(Attributes.ARMOR) && modifier.id().equals(armor.get(0).id())) {
                output.accept(attribute, new AttributeModifier(modifier.id(), toughnessAmount, modifier.operation()));
                if (toughness.isEmpty()) {
                    output.accept(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Hexed.id("displaced_toughness"), armorAmount, AttributeModifier.Operation.ADD_VALUE));
                }
            } else if (attribute.is(Attributes.ARMOR_TOUGHNESS) && !toughness.isEmpty() && modifier.id().equals(toughness.get(0).id())) {
                output.accept(attribute, new AttributeModifier(modifier.id(), armorAmount, modifier.operation()));
            } else {
                output.accept(attribute, modifier);
            }
        };
    }

    @Inject(method = "mineBlock", at = @At("HEAD"))
    private void hexed$postMine(Level level, BlockState state, BlockPos pos, Player player, CallbackInfo ci) {
        Hexed.onPostMine(hexed$self(), level, state, pos, player);
    }

    /** Famishment tools take their durability cost out of the user's hunger instead. */
    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
        at = @At("HEAD"), cancellable = true)
    private void hexed$famishment(int damage, ServerLevel level, @Nullable ServerPlayer player, Consumer<Item> onBreak, CallbackInfo ci) {
        if (player == null || damage <= 0 || !isDamageableItem() || player.hasInfiniteMaterials()
            || !HexHelper.has(hexed$self(), Hexes.FAMISHMENT)) return;

        if (player.getFoodData().getFoodLevel() >= damage) {
            player.causeFoodExhaustion(damage);
        } else if (!HexHelper.hasFullRobes(player)) {
            player.hurt(player.damageSources().dryOut(), damage);
        }
        ci.cancel();
    }
}
