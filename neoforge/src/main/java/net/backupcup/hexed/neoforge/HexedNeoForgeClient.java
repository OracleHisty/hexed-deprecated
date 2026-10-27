package net.backupcup.hexed.neoforge;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.client.HexedHud;
import net.backupcup.hexed.client.render.AccursedAltarRunesRenderer;
import net.backupcup.hexed.client.render.BlazingSkullModel;
import net.backupcup.hexed.client.render.BlazingSkullRenderer;
import net.backupcup.hexed.client.screen.AccursedAltarScreen;
import net.backupcup.hexed.register.*;
import net.backupcup.hexed.registry.RegObject;
import net.backupcup.hexed.util.CustomHandTexture;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = Hexed.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HexedNeoForgeClient {
    private static final ResourceLocation MAGMA_STILL = Hexed.id("block/blazing_magma_still");
    private static final ResourceLocation MAGMA_FLOW = Hexed.id("block/blazing_magma_flow");

    private HexedNeoForgeClient() {}

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (RegObject<? extends Block> block : RegisterBlocks.cutoutBlocks()) {
                ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout());
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(RegisterMenus.ACCURSED_ALTAR.get(), AccursedAltarScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(RegisterBlockEntities.ACCURSED_ALTAR.get(), AccursedAltarRunesRenderer::new);
        event.registerEntityRenderer(RegisterEntities.BLAZING_SKULL.get(), BlazingSkullRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BlazingSkullModel.LAYER, BlazingSkullModel::createLayer);
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        for (RegObject<Item> item : RegisterItems.tainted()) {
            if (item.get() instanceof CustomHandTexture hand) event.register(ModelResourceLocation.standalone(hand.getHandModel()));
        }
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Hexed.id("hex_hud"), HexedHud::render);
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return MAGMA_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return MAGMA_FLOW;
            }
        }, HexedNeoForgeFluids.BLAZING_MAGMA_TYPE.get());
    }
}
