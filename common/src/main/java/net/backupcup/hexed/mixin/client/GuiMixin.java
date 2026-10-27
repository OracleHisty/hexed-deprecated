package net.backupcup.hexed.mixin.client;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.registry.RegObject;
import net.backupcup.hexed.register.RegisterStatusEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(value = Gui.class, priority = 10)
public abstract class GuiMixin {
    @Shadow private ItemStack lastToolHighlight;

    @Unique
    private static final Map<RegObject<MobEffect>, ResourceLocation> HEXED$HEART_TEXTURES = new LinkedHashMap<>();

    static {
        HEXED$HEART_TEXTURES.put(RegisterStatusEffects.SMOULDERING, Hexed.id("textures/gui/icons_smouldering.png"));
        HEXED$HEART_TEXTURES.put(RegisterStatusEffects.BLIGHTED, Hexed.id("textures/gui/icons_smouldering.png"));
        HEXED$HEART_TEXTURES.put(RegisterStatusEffects.AFLAME, Hexed.id("textures/gui/icons_aflame.png"));
        HEXED$HEART_TEXTURES.put(RegisterStatusEffects.ABLAZE, Hexed.id("textures/gui/icons_aflame.png"));
        HEXED$HEART_TEXTURES.put(RegisterStatusEffects.TRAITOROUS, Hexed.id("textures/gui/icons_traitorous.png"));
    }

    /** Effects that block healing replace the normal hearts with their own. */
    @Inject(method = "renderHeart", at = @At("HEAD"), cancellable = true)
    private void hexed$drawEffectHearts(GuiGraphics graphics, Gui.HeartType type, int x, int y, boolean hardcore, boolean blinking,
                                        boolean halfHeart, CallbackInfo ci) {
        if (blinking || type != Gui.HeartType.NORMAL || !(Minecraft.getInstance().cameraEntity instanceof Player player)) return;

        for (Map.Entry<RegObject<MobEffect>, ResourceLocation> entry : HEXED$HEART_TEXTURES.entrySet()) {
            if (player.hasEffect(entry.getKey().holder())) {
                graphics.blit(entry.getValue(), x, y, halfHeart ? 9 : 0, hardcore ? 45 : 0, 9, 9);
                ci.cancel();
                return;
            }
        }
    }

    // NeoForge moves the body of this method into an overload; the wildcard matches every overload.
    @ModifyVariable(method = "renderSelectedItemName*", at = @At("STORE"), ordinal = 0)
    private MutableComponent hexed$modifyHeldItemName(MutableComponent text) {
        return HexHelper.modifyTooltip(text, lastToolHighlight);
    }
}
