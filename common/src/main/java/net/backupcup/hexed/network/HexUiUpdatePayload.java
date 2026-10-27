package net.backupcup.hexed.network;

import net.backupcup.hexed.Hexed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** State for one of the charge HUDs. {@code active} is only used by Provision. */
public record HexUiUpdatePayload(Ui ui, boolean active, int value) implements CustomPacketPayload {
    public enum Ui {
        PROVISION,
        OVERCLOCK,
        AGGRAVATE,
        PHASED
    }

    public static final Type<HexUiUpdatePayload> TYPE = new Type<>(Hexed.id("hex_ui_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HexUiUpdatePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.idMapper(i -> Ui.values()[i], Ui::ordinal), HexUiUpdatePayload::ui,
        ByteBufCodecs.BOOL, HexUiUpdatePayload::active,
        ByteBufCodecs.VAR_INT, HexUiUpdatePayload::value,
        HexUiUpdatePayload::new
    );

    public static HexUiUpdatePayload of(Ui ui, int value) {
        return new HexUiUpdatePayload(ui, false, value);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
