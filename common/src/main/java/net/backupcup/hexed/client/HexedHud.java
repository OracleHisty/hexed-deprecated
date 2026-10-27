package net.backupcup.hexed.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.backupcup.hexed.item.BishopLabrysItem;
import net.backupcup.hexed.network.HexUiUpdatePayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

/** The crosshair overlays for the charge-based hexes and the Bishop's Labrys. */
public final class HexedHud {
    private static final ResourceLocation AGGRAVATE_TEXTURE = Hexed.id("textures/gui/aggravate_ui.png");
    private static final ResourceLocation PHASED_TEXTURE = Hexed.id("textures/gui/phased_ui.png");
    private static final ResourceLocation OVERCLOCK_TEXTURE = Hexed.id("textures/gui/overclock_ui.png");
    private static final ResourceLocation PROVISION_TEXTURE = Hexed.id("textures/gui/provision_ui.png");
    private static final ResourceLocation LABRYS_TEXTURE = Hexed.id("textures/gui/bishop_labrys_ui.png");

    private static int aggravateCharge = 0;
    private static int phasedCharge = 0;
    private static int overclockCharge = 0;
    private static boolean provisionActive = false;
    private static int provisionIndicator = 0;

    private HexedHud() {}

    public static void update(HexUiUpdatePayload payload) {
        switch (payload.ui()) {
            case AGGRAVATE -> aggravateCharge = payload.value();
            case PHASED -> phasedCharge = payload.value();
            case OVERCLOCK -> overclockCharge = payload.value();
            case PROVISION -> {
                provisionActive = payload.active();
                provisionIndicator = payload.value();
            }
        }
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.isPaused() || player.isSpectator()) return;

        RenderSystem.enableBlend();

        ItemStack bow = player.getMainHandItem().getItem() instanceof BowItem ? player.getMainHandItem()
            : player.getOffhandItem().getItem() instanceof BowItem ? player.getOffhandItem() : ItemStack.EMPTY;
        if (HexHelper.has(bow, Hexes.AGGRAVATE)) drawAggravate(graphics);
        if (HexHelper.has(bow, Hexes.PHASED)) drawPhased(graphics);

        ItemStack crossbow = player.getMainHandItem().getItem() instanceof CrossbowItem ? player.getMainHandItem() : ItemStack.EMPTY;
        if (HexHelper.has(crossbow, Hexes.OVERCLOCK)) drawOverclock(graphics);
        if (HexHelper.has(crossbow, Hexes.PROVISION)) {
            if (provisionActive && !CrossbowItem.isCharged(crossbow)) drawProvision(graphics);
            else provisionActive = false;
        }

        ItemStack labrys = player.getMainHandItem().getItem() instanceof BishopLabrysItem ? player.getMainHandItem()
            : player.getOffhandItem().getItem() instanceof BishopLabrysItem ? player.getOffhandItem() : ItemStack.EMPTY;
        if (!labrys.isEmpty()) drawLabrys(graphics, BishopLabrysItem.getCharge(labrys));

        RenderSystem.disableBlend();
    }

    private static void drawAggravate(GuiGraphics graphics) {
        int x = (graphics.guiWidth() - 39) / 2;
        int y = (graphics.guiHeight() + 18) / 2;
        if (aggravateCharge < 7) {
            graphics.blit(AGGRAVATE_TEXTURE, x, y, 0f, 0f, 39, 18, 256, 256);
            graphics.blit(AGGRAVATE_TEXTURE, x + 5, y + 5, 5f, 24f, 5 * aggravateCharge, 8, 256, 256);
        } else {
            graphics.blit(AGGRAVATE_TEXTURE, x, y - 1, 0f, 18f, 44, 21, 256, 256);
        }
    }

    private static void drawPhased(GuiGraphics graphics) {
        int x = (graphics.guiWidth() - 39) / 2;
        int y = (graphics.guiHeight() - 41) / 2;
        graphics.blit(PHASED_TEXTURE, x, y, 0f, 0f, 39, 41, 256, 256);
        graphics.blit(PHASED_TEXTURE, x, y, 39f * phasedCharge, 0f, 39, 41, 256, 256);
    }

    private static void drawOverclock(GuiGraphics graphics) {
        int x = (graphics.guiWidth() - 37) / 2;
        int y = (graphics.guiHeight() - 37) / 2;
        graphics.blit(OVERCLOCK_TEXTURE, x, y, 0f, 0f, 37, 37, 256, 256);

        // Each frame covers a band of overheat; the bands get narrower as it heats up.
        int u, v;
        if (overclockCharge <= 3) { u = 37; v = 0; }
        else if (overclockCharge <= 7) { u = 74; v = 0; }
        else if (overclockCharge <= 10) { u = 111; v = 0; }
        else if (overclockCharge <= 13) { u = 148; v = 0; }
        else if (overclockCharge <= 15) { u = 37; v = 37; }
        else if (overclockCharge <= 17) { u = 74; v = 37; }
        else if (overclockCharge == 18) { u = 111; v = 37; }
        else { u = 148; v = 37; }
        graphics.blit(OVERCLOCK_TEXTURE, x, y, u, v, 37, 37, 256, 256);
    }

    private static void drawProvision(GuiGraphics graphics) {
        int x = (graphics.guiWidth() - 94) / 2;
        int y = (graphics.guiHeight() + 19) / 2;
        graphics.blit(PROVISION_TEXTURE, x, y, 0f, 0f, 94, 19, 256, 256);

        float cursorU = 2f;
        if (provisionIndicator >= 17 && provisionIndicator <= 35) cursorU = 17f;
        else if ((provisionIndicator >= 10 && provisionIndicator <= 16) || (provisionIndicator >= 36 && provisionIndicator <= 43)) cursorU = 32f;
        graphics.blit(PROVISION_TEXTURE, x + provisionIndicator + 3, y - 1, cursorU, 21f, 12, 21, 256, 256);
    }

    private static void drawLabrys(GuiGraphics graphics, int charge) {
        int x = (graphics.guiWidth() - 43) / 2;
        int y = (graphics.guiHeight() + 17 * 2) / 2;
        graphics.blit(LABRYS_TEXTURE, x, y, 0f, 0f, 43, 17, 256, 256);
        graphics.blit(LABRYS_TEXTURE, x, y, 0f, 17f * charge, 43, 17, 256, 256);
    }
}
