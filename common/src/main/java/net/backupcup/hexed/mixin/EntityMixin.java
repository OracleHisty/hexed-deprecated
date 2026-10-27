package net.backupcup.hexed.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.backupcup.hexed.flame.HexedFlames;
import net.backupcup.hexed.util.AflameBurning;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract boolean isSpectator();

    /** Aflame creatures are drawn burning even when they aren't on fire, and even if they're fire immune. */
    @ModifyReturnValue(method = "displayFireAnimation", at = @At("RETURN"))
    private boolean hexed$showAflame(boolean original) {
        return original || ((Object) this instanceof AflameBurning aflame && aflame.hexed$isAflameBurning() && !isSpectator());
    }

    /** Blazing Magma burns like lava (through the lava fluid tag), with its own flames. */
    @Inject(method = "lavaHurt", at = @At("TAIL"))
    private void hexed$blazingMagmaFlames(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide() && self.isOnFire() && HexedFlames.isInBlazingMagma(self)) {
            HexedFlames.show(self, HexedFlames.Flame.BLAZING_MAGMA);
        }
    }
}
