package net.backupcup.hexed.network;

import net.backupcup.hexed.Hexed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The server's config as JSON, so clients agree with it on hex behavior. */
public record SyncConfigPayload(String json) implements CustomPacketPayload {
    public static final Type<SyncConfigPayload> TYPE = new Type<>(Hexed.id("sync_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncConfigPayload> CODEC =
        ByteBufCodecs.stringUtf8(Integer.MAX_VALUE / 4).map(SyncConfigPayload::new, SyncConfigPayload::json).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
