package net.backupcup.hexed.item;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.util.CustomHandTexture;
import net.backupcup.hexed.util.TaintedItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Shared base for the tainted swords: a custom in-hand model and a lore line. */
public abstract class TaintedSwordItem extends SwordItem implements CustomHandTexture, TaintedItem {
    private final String itemId;
    /** Matches the old Yarn {@code SwordItem#getAttackDamage}: tier bonus plus the item's own damage. */
    protected final float attackDamage;
    protected final float attackSpeed;

    protected TaintedSwordItem(Tier tier, int attackDamage, float attackSpeed, String itemId, Properties properties) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, attackDamage, attackSpeed)));
        this.itemId = itemId;
        this.attackDamage = attackDamage + tier.getAttackDamageBonus();
        this.attackSpeed = attackSpeed;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    public float getAttackSpeed() {
        return attackSpeed;
    }

    @Override
    public ResourceLocation getHandModel() {
        return Hexed.id("item/" + itemId + "_hand");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hexed." + itemId).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
