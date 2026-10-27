package net.backupcup.hexed.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

/** A button drawn from a region of a 256x256 texture, with the hovered state {@code hoverOffset} pixels below. */
public class TexturedButton extends Button {
    private final ResourceLocation texture;
    private final int u;
    private final int v;
    private final int hoverOffset;

    public TexturedButton(int width, int height, int u, int v, int hoverOffset, ResourceLocation texture, OnPress onPress) {
        super(0, 0, width, height, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.hoverOffset = hoverOffset;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int textureV = isHoveredOrFocused() ? v + hoverOffset : v;
        graphics.blit(texture, getX(), getY(), u, textureV, width, height);
    }
}
