package net.backupcup.hexed.network;

import net.backupcup.hexed.Hexed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** The hexes the altar offers for the item in its gear slot, and which one is selected. */
public record AltarHexesPayload(List<ResourceLocation> hexes, int current) implements CustomPacketPayload {
    public static final Type<AltarHexesPayload> TYPE = new Type<>(Hexed.id("altar_hexes"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AltarHexesPayload> CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), AltarHexesPayload::hexes,
        ByteBufCodecs.VAR_INT, AltarHexesPayload::current,
        AltarHexesPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
