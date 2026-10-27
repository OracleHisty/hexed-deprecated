package net.backupcup.hexed.neoforge;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.client.HexedClientNetwork;
import net.backupcup.hexed.entity.BlazingSkullEntity;
import net.backupcup.hexed.flame.HexedFlames;
import net.backupcup.hexed.loot.HexedLoot;
import net.backupcup.hexed.network.HexedNetwork;
import net.backupcup.hexed.register.RegisterEntities;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Hexed.MOD_ID)
public class HexedNeoForge {
    public HexedNeoForge(IEventBus modBus) {
        Hexed.init();
        HexedNeoForgeFluids.init();
        // mods.toml can't express "one of these two", so it is checked here.
        if (HexedFlames.activeMod() == null) {
            throw new ModLoadingException(ModLoadingIssue.error(HexedFlames.missingMessage()));
        }

        modBus.addListener(RegisterEvent.class, NeoForgePlatform::onRegister);
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(HexedFlames::registerFlames));
        modBus.addListener(RegisterPayloadHandlersEvent.class, HexedNeoForge::registerPayloads);
        modBus.addListener(EntityAttributeCreationEvent.class,
            event -> event.put(RegisterEntities.BLAZING_SKULL.get(), BlazingSkullEntity.createAttributes().build()));

        IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(ServerTickEvent.Pre.class, event -> Hexed.onServerTick());
        bus.addListener(ServerStoppedEvent.class, event -> Hexed.onServerStopped());
        bus.addListener(OnDatapackSyncEvent.class, event -> {
            boolean joined = event.getPlayer() != null;
            event.getRelevantPlayers().forEach(player -> Hexed.onDatapackSync(player, joined));
        });
        bus.addListener(AddReloadListenerEvent.class, event -> event.addListener(Hexed.CONFIG_RELOADER));
        bus.addListener(LootTableLoadEvent.class, event ->
            HexedLoot.modify(event.getKey(), pool -> event.getTable().addPool(pool.build())));
        bus.addListener(EntityTickEvent.Pre.class, HexedNeoForge::blazingMagmaBurns);
    }

    /**
     * NeoForge decides "in lava" by fluid type rather than the {@code minecraft:lava} tag, so Blazing Magma's own
     * fluid type has to burn things itself, like lava does in {@code Entity#baseTick}.
     */
    private static void blazingMagmaBurns(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        if (!entity.level().isClientSide() && entity.isInFluidType(HexedNeoForgeFluids.BLAZING_MAGMA_TYPE.get())) {
            entity.lavaHurt();
            entity.fallDistance *= 0.5f;
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        for (HexedNetwork.Entry<?> entry : HexedNetwork.CLIENTBOUND) {
            registerClientbound(registrar, entry);
        }
        registrar.playToServer(HexedNetwork.PULL_PREDICATE.type(), HexedNetwork.PULL_PREDICATE.codec(), (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) HexedNetwork.handlePullPredicate(payload, player);
        });
    }

    private static <T extends CustomPacketPayload> void registerClientbound(PayloadRegistrar registrar, HexedNetwork.Entry<T> entry) {
        registrar.playToClient(entry.type(), entry.codec(), (payload, context) -> HexedClientNetwork.handle(payload));
    }
}
