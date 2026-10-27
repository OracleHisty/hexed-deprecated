package net.backupcup.hexed.network;

import net.backupcup.hexed.Hexed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** How far the client's bow or crossbow is drawn (its {@code pull} model property). Only the client knows it. */
public record PullPredicatePayload(float pull) implements CustomPacketPayload {
    public static final Type<PullPredicatePayload> TYPE = new Type<>(Hexed.id("pull_predicate"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PullPredicatePayload> CODEC =
        ByteBufCodecs.FLOAT.map(PullPredicatePayload::new, PullPredicatePayload::pull).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
