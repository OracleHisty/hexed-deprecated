package net.backupcup.hexed.util;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/** State the bow hexes attach to arrows, mixed into {@code AbstractArrow}. */
public interface HexProjectile {
    boolean hexed$isVolatile();

    void hexed$setVolatile(boolean value);

    /** Resentful scales damage by the distance to whoever fired the arrow. */
    void hexed$setResentfulOwner(@Nullable Entity owner);
}
