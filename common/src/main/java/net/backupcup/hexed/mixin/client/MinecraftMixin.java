package net.backupcup.hexed.mixin.client;

import net.backupcup.hexed.util.ItemUseCooldown;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements ItemUseCooldown {
    @Shadow private int rightClickDelay;

    @Override
    public void hexed$imposeItemUseCooldown(int ticks) {
        rightClickDelay = ticks;
    }
}
