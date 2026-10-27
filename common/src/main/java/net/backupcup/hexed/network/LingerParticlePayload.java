package net.backupcup.hexed.network;

import net.backupcup.hexed.Hexed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public record LingerParticlePayload(Vec3 pos) implements CustomPacketPayload {
    public static final Type<LingerParticlePayload> TYPE = new Type<>(Hexed.id("linger_particle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LingerParticlePayload> CODEC =
        HexedCodecs.VEC3.map(LingerParticlePayload::new, LingerParticlePayload::pos).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
