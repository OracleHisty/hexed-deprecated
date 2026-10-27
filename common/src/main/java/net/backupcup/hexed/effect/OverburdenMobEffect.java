package net.backupcup.hexed.effect;

import net.backupcup.hexed.Hexed;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Slows the miner down while making each level mine faster. The mining bonus comes from the config. */
public class OverburdenMobEffect extends HexMobEffect {
    private static final ResourceLocation BREAK_SPEED_ID = Hexed.id("effect.overburden.break_speed");

    public OverburdenMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(attributes, amplifier);
        AttributeInstance breakSpeed = attributes.getInstance(Attributes.BLOCK_BREAK_SPEED);
        if (breakSpeed != null) {
            breakSpeed.removeModifier(BREAK_SPEED_ID);
            breakSpeed.addPermanentModifier(new AttributeModifier(BREAK_SPEED_ID,
                amplifier * Hexed.config().overburdenHex.movementSpeedModifier, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributes) {
        super.removeAttributeModifiers(attributes);
        AttributeInstance breakSpeed = attributes.getInstance(Attributes.BLOCK_BREAK_SPEED);
        if (breakSpeed != null) breakSpeed.removeModifier(BREAK_SPEED_ID);
    }
}
