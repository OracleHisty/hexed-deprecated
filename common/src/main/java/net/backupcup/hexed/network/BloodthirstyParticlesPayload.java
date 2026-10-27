package net.backupcup.hexed.network;

import net.backupcup.hexed.Hexed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public record BloodthirstyParticlesPayload(int amount, Vec3 pos) implements CustomPacketPayload {
    public static final Type<BloodthirstyParticlesPayload> TYPE = new Type<>(Hexed.id("bloodthirsty_particles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BloodthirstyParticlesPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, BloodthirstyParticlesPayload::amount,
        HexedCodecs.VEC3, BloodthirstyParticlesPayload::pos,
        BloodthirstyParticlesPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
