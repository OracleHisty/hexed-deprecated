package net.backupcup.hexed.network;

import net.backupcup.hexed.Hexed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** A Blazing Basher wave: the ring around {@code center} plus a burst on every creature it hit. */
public record BasherParticlesPayload(double radius, Vec3 center, List<Vec3> hits) implements CustomPacketPayload {
    public static final Type<BasherParticlesPayload> TYPE = new Type<>(Hexed.id("basher_particles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BasherParticlesPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, BasherParticlesPayload::radius,
        HexedCodecs.VEC3, BasherParticlesPayload::center,
        HexedCodecs.VEC3.apply(ByteBufCodecs.list()), BasherParticlesPayload::hits,
        BasherParticlesPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
