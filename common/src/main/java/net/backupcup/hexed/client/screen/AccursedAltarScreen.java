package net.backupcup.hexed.client.screen;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.altar.AccursedAltarMenu;
import net.backupcup.hexed.hex.Hexes;
import net.backupcup.hexed.util.TextWrapUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccursedAltarScreen extends AbstractContainerScreen<AccursedAltarMenu> {
    private static final ResourceLocation TEXTURE = Hexed.id("textures/gui/accursed_altar.png");
    private static final int WHITE = 0xFFFFFFFF;

    private static final Map<ResourceKey<Enchantment>, String> ULTRAKILL_REFERENCES = Map.of(
        Hexes.METAMORPHOSIS, "tooltip.hexed.ultrakill.mankind_is_dead",
        Hexes.BLOODTHIRSTY, "tooltip.hexed.ultrakill.blood_is_fuel",
        Hexes.IRONCLAD, "tooltip.hexed.ultrakill.hell_is_full"
    );

    private List<ResourceLocation> availableHexes = List.of();
    private int currentHex = 0;

    private TexturedButton buttonHex;
    private TexturedButton buttonScrollUp;
    private TexturedButton buttonScrollDown;

    public AccursedAltarScreen(AccursedAltarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 196;
        imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelY = 1000;
        inventoryLabelY = 1000;

        buttonHex = addRenderableWidget(new TexturedButton(24, 29, 231, 32, 31, TEXTURE, button -> {
            clickButton(AccursedAltarMenu.BUTTON_HEX);
            onClose();
        }));
        buttonHex.setPosition(leftPos + 14, topPos + 45);

        buttonScrollUp = addRenderableWidget(new TexturedButton(74, 9, 1, 236, 10, TEXTURE,
            button -> clickButton(AccursedAltarMenu.BUTTON_SCROLL_UP)));
        buttonScrollUp.setPosition(leftPos + 85, topPos + 73);

        buttonScrollDown = addRenderableWidget(new TexturedButton(74, 9, 77, 236, 10, TEXTURE,
            button -> clickButton(AccursedAltarMenu.BUTTON_SCROLL_DOWN)));
        buttonScrollDown.setPosition(leftPos + 85, topPos + 98);
    }

    private void clickButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    public void updateHexes(List<ResourceLocation> hexes, int current) {
        availableHexes = hexes;
        currentHex = hexes.isEmpty() ? 0 : Math.floorMod(current, hexes.size());
    }

    private boolean canUse() {
        return !menu.getGearSlot().isEmpty() && !menu.getMaterialSlot().isEmpty() && menu.isAltarActive() && !availableHexes.isEmpty();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean usable = canUse();
        for (TexturedButton button : List.of(buttonHex, buttonScrollUp, buttonScrollDown)) {
            button.active = usable;
            button.visible = usable;
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        if (usable) {
            // Arrows
            graphics.blit(TEXTURE, leftPos + 44, topPos + 23, 230, 94, 15, 10);
            graphics.blit(TEXTURE, leftPos + 43, topPos + 85, 214, 94, 15, 10);

            ResourceLocation current = availableHexes.get(currentHex);
            List<ResourceLocation> shown = neighbouringHexes();
            for (int i = 0; i < shown.size(); i++) {
                ResourceLocation hex = shown.get(i);
                boolean selected = i == 1;
                graphics.blit(TEXTURE, leftPos + 70, topPos + 21 + 17 * i, 152, selected ? 239 : 222, 104, 16);
                graphics.blit(TEXTURE, leftPos + 73, topPos + 25 + 17 * i, 246, 94, 9, 8);

                Component name = Component.translatable(Hexes.translationKey(hex));
                Component text = selected ? Component.literal("▶ ").append(name).append(Component.literal(" ◀")) : name;
                graphics.drawString(font, text, leftPos + 85, topPos + 25 + 17 * i, WHITE, true);
            }

            graphics.blit(TEXTURE, leftPos + 72, topPos + 86, 246, 94, 9, 8);
            graphics.drawString(font, Component.translatable(Hexes.translationKey(current)), leftPos + 84, topPos + 86, WHITE, true);

            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, current);
            graphics.blit(Hexes.runeTexture(key), leftPos + 47, topPos + 51, 0f, 0f, 16, 16, 16, 16);
            renderHexTooltip(graphics, key, mouseX, mouseY);
        }

        renderTooltip(graphics, mouseX, mouseY);
    }

    /** The previous, current and next hex, wrapping around the list. */
    private List<ResourceLocation> neighbouringHexes() {
        int size = availableHexes.size();
        return List.of(
            availableHexes.get(Math.floorMod(currentHex - 1, size)),
            availableHexes.get(currentHex),
            availableHexes.get(Math.floorMod(currentHex + 1, size)));
    }

    private void renderHexTooltip(GuiGraphics graphics, ResourceKey<Enchantment> hex, int mouseX, int mouseY) {
        if (!isInBounds(mouseX, mouseY, leftPos + 47, topPos + 51, 16, 16)) return;

        String translationKey = Hexes.translationKey(hex.location());
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(translationKey).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        lines.addAll(TextWrapUtils.wrapText(width, translationKey + ".desc", ChatFormatting.GRAY));

        //ULTRAKILL reference
        String reference = ULTRAKILL_REFERENCES.get(hex);
        if (reference != null) {
            lines.add(Component.translatable(reference).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        }

        graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
    }

    private static boolean isInBounds(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
