package net.backupcup.mixin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import java.util.Map.Entry;
import kotlin.random.Random;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.enchantments.AbstractHex;
import net.backupcup.hexed.register.RegisterEnchantments;
import net.backupcup.hexed.register.RegisterStatusEffects;
import net.backupcup.hexed.util.AttributeProviding;
import net.backupcup.hexed.util.HexHelper;
import net.backupcup.hexed.util.HexRandom;
import net.backupcup.hexed.util.TaintedItem;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.backupcup.hexed.api.HexedEvents.PostMine.POST_MINE;
import static net.minecraft.item.ItemStack.ENCHANTMENTS_KEY;

@Mixin(value = ItemStack.class, priority = 10)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();

    @Shadow public abstract NbtList getEnchantments();

    @Shadow public abstract Text getName();

    @Shadow private @Nullable NbtCompound nbt;

    @Unique

    @ModifyVariable(method = "getTooltip", at = @At("STORE"), ordinal = 0)
    private MutableText hexed$modifyTooltipFormat(MutableText text) {
        ItemStack itemStack = (ItemStack) (Object) this;

        return HexHelper.modifyTooltip(text, itemStack);
    }

    @ModifyReturnValue(method = "hasEnchantments", at = @At("RETURN"))
    private boolean hexed$EnchantableHexes(boolean original) {
        if (nbt != null && nbt.contains(ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) {
            return !HexHelper.INSTANCE.getEnchantments((ItemStack) (Object) this).stream().filter(it -> !(it instanceof AbstractHex)).toList().isEmpty();
        }
        return false;
    }

    @ModifyReturnValue(method = "getAttributeModifiers", at = @At("RETURN"))
    private Multimap<EntityAttribute, EntityAttributeModifier> hexed$DisplacedSwapAttributes(
            Multimap<EntityAttribute, EntityAttributeModifier> original, EquipmentSlot slot) {
        if (getItem() instanceof EnchantedBookItem) return original;
        if (getItem() instanceof Equipment equipment && equipment.getSlotType() != slot) return original;

        Multimap<EntityAttribute, EntityAttributeModifier> newMap = null;
        boolean modified = false;

        for (var entry: EnchantmentHelper.get((ItemStack) (Object) this).entrySet()) {
            if (entry.getKey() instanceof AttributeProviding attributeProvidingEnchant) {
                if (newMap == null) newMap = ArrayListMultimap.create(original);
                attributeProvidingEnchant.modifyAttributeMap(newMap, slot, entry.getValue());
                modified = true;
            }
        }

        return modified ? newMap : original;
    }

    @Inject(method = "postMine", at = @At("HEAD"))
    private void hexed$AmplifyMine(World world, BlockState state, BlockPos pos, PlayerEntity player, CallbackInfo ci) {
        POST_MINE.invoker().postMine((ItemStack) (Object) this, world, state, pos, player);
    }

    @WrapWithCondition(method = "damage(ILnet/minecraft/util/math/random/Random;Lnet/minecraft/server/network/ServerPlayerEntity;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setDamage(I)V"))
    private boolean hexed$FamishmentShouldDamage(ItemStack stack, int amount, int damage, net.minecraft.util.math.random.Random random, ServerPlayerEntity player) {
        if (HexHelper.INSTANCE.stackHasEnchantment(stack, RegisterEnchantments.INSTANCE.getFAMISHMENT_HEX())) {
            if (player == null) return true;

            if (player.getHungerManager().getFoodLevel() >= damage) {
                player.getHungerManager().addExhaustion(damage);
            } else if (!HexHelper.INSTANCE.hasFullRobes(player)){
                player.damage(player.getDamageSources().dryOut(), damage);
            }
            return false;
        }
        return true;
    }
}
