package net.backupcup.hexed.fabric;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.entity.BlazingSkullEntity;
import net.backupcup.hexed.loot.HexedLoot;
import net.backupcup.hexed.network.HexedNetwork;
import net.backupcup.hexed.register.RegisterEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class HexedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Hexed.init();

        for (HexedNetwork.Entry<?> entry : HexedNetwork.CLIENTBOUND) {
            registerClientbound(entry);
        }
        PayloadTypeRegistry.playC2S().register(HexedNetwork.PULL_PREDICATE.type(), HexedNetwork.PULL_PREDICATE.codec());
        ServerPlayNetworking.registerGlobalReceiver(HexedNetwork.PULL_PREDICATE.type(),
            (payload, context) -> HexedNetwork.handlePullPredicate(payload, context.player()));

        FabricDefaultAttributeRegistry.register(RegisterEntities.BLAZING_SKULL.get(), BlazingSkullEntity.createAttributes());

        ServerTickEvents.START_SERVER_TICK.register(server -> Hexed.onServerTick());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> Hexed.onServerStopped());
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(Hexed::onDatapackSync);

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return Hexed.id("config");
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                Hexed.CONFIG_RELOADER.onResourceManagerReload(manager);
            }
        });

        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if (source.isBuiltin()) HexedLoot.modify(key, builder::withPool);
        });
    }

    private static <T extends CustomPacketPayload> void registerClientbound(HexedNetwork.Entry<T> entry) {
        PayloadTypeRegistry.playS2C().register(entry.type(), entry.codec());
    }
}
