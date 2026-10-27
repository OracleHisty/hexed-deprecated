package net.backupcup.hexed.network;

import net.backupcup.hexed.util.HexPlayerData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.List;

/**
 * Payload definitions and sending helpers. Sending goes through vanilla packets, so only registering the payload
 * types and their handlers is loader specific.
 */
public final class HexedNetwork {
    public record Entry<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {}

    /** Server to client. Handled by {@code HexedClientNetwork}. */
    public static final List<Entry<?>> CLIENTBOUND = List.of(
        new Entry<>(SyncConfigPayload.TYPE, SyncConfigPayload.CODEC),
        new Entry<>(AltarHexesPayload.TYPE, AltarHexesPayload.CODEC),
        new Entry<>(HexUiUpdatePayload.TYPE, HexUiUpdatePayload.CODEC),
        new Entry<>(BloodthirstyParticlesPayload.TYPE, BloodthirstyParticlesPayload.CODEC),
        new Entry<>(LingerParticlePayload.TYPE, LingerParticlePayload.CODEC),
        new Entry<>(BasherParticlesPayload.TYPE, BasherParticlesPayload.CODEC)
    );

    /** Client to server. */
    public static final Entry<PullPredicatePayload> PULL_PREDICATE = new Entry<>(PullPredicatePayload.TYPE, PullPredicatePayload.CODEC);

    private HexedNetwork() {}

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        player.connection.send(new ClientboundCustomPayloadPacket(payload));
    }

    /** Sends to every player tracking {@code entity}, and to the entity itself if it is a player. */
    public static void sendToTracking(Entity entity, CustomPacketPayload payload) {
        if (entity.level() instanceof ServerLevel level) {
            level.getChunkSource().broadcastAndSend(entity, new ClientboundCustomPayloadPacket(payload));
        }
    }

    public static void handlePullPredicate(PullPredicatePayload payload, ServerPlayer player) {
        ((HexPlayerData) player).hexed$setPullPredicate(payload.pull());
    }
}
