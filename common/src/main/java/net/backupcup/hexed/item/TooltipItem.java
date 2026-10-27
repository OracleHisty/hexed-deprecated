package net.backupcup.hexed.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TooltipItem extends Item {
    private final List<Component> lines;

    public TooltipItem(Properties properties, List<Component> lines) {
        super(properties);
        this.lines = lines;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.addAll(lines);
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
