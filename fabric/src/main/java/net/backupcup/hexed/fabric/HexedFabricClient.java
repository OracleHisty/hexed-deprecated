package net.backupcup.hexed.fabric;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.client.HexedClientNetwork;
import net.backupcup.hexed.client.HexedHud;
import net.backupcup.hexed.client.render.AccursedAltarRunesRenderer;
import net.backupcup.hexed.client.render.BlazingSkullModel;
import net.backupcup.hexed.client.render.BlazingSkullRenderer;
import net.backupcup.hexed.client.screen.AccursedAltarScreen;
import net.backupcup.hexed.network.HexedNetwork;
import net.backupcup.hexed.register.*;
import net.backupcup.hexed.registry.RegObject;
import net.backupcup.hexed.util.CustomHandTexture;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class HexedFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        for (HexedNetwork.Entry<?> entry : HexedNetwork.CLIENTBOUND) {
            registerReceiver(entry);
        }

        MenuScreens.register(RegisterMenus.ACCURSED_ALTAR.get(), AccursedAltarScreen::new);
        BlockEntityRenderers.register(RegisterBlockEntities.ACCURSED_ALTAR.get(), AccursedAltarRunesRenderer::new);
        EntityRendererRegistry.register(RegisterEntities.BLAZING_SKULL.get(), BlazingSkullRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(BlazingSkullModel.LAYER, BlazingSkullModel::createLayer);

        for (RegObject<? extends Block> block : RegisterBlocks.cutoutBlocks()) {
            BlockRenderLayerMap.INSTANCE.putBlock(block.get(), RenderType.cutout());
        }

        FluidRenderHandlerRegistry.INSTANCE.register(RegisterSlagBlocks.BLAZING_MAGMA_SOURCE.get(), RegisterSlagBlocks.FLOWING_BLAZING_MAGMA.get(),
            new SimpleFluidRenderHandler(Hexed.id("block/blazing_magma_still"), Hexed.id("block/blazing_magma_flow")));

        ModelLoadingPlugin.register(context -> {
            for (RegObject<Item> item : RegisterItems.tainted()) {
                if (item.get() instanceof CustomHandTexture hand) context.addModels(hand.getHandModel());
            }
        });

        HudRenderCallback.EVENT.register(HexedHud::render);
    }

    private static <T extends CustomPacketPayload> void registerReceiver(HexedNetwork.Entry<T> entry) {
        ClientPlayNetworking.registerGlobalReceiver(entry.type(), (payload, context) -> HexedClientNetwork.handle(payload));
    }
}
