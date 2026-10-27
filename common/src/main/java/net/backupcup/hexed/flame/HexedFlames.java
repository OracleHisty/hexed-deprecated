package net.backupcup.hexed.flame;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.platform.Services;
import net.backupcup.hexed.register.RegisterSlagBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * Custom flame textures for burning entities. The textures are drawn by a flame mod: Prometheus is preferred, and
 * Dyed Flames is the fallback. Hexed requires exactly one of them, see {@link #missingMessage()}.
 */
public final class HexedFlames {
    public static final String PROMETHEUS = "prometheus";
    public static final String DYED_FLAMES = "dyedflames";

    /** Each flame's textures are {@code hexed:block/<name>_fire_0} and {@code _fire_1}. */
    public enum Flame {
        AFLAME("aflame"),
        BLAZING_MAGMA("blazing_magma");

        private final ResourceLocation id;

        Flame(String name) {
            this.id = Hexed.id(name);
        }

        public ResourceLocation id() {
            return id;
        }
    }

    /** How one flame mod is told which flame an entity shows. */
    public interface Backend {
        /** Called once during mod initialization. */
        void register();

        /** Makes the entity's flames use this flame's textures. Must be cheap to repeat. */
        void show(Entity entity, Flame flame);

        /** Reverts the entity to its normal flames, if it is still showing this flame. */
        void clear(Entity entity, Flame flame);
    }

    private static final String PROMETHEUS_BACKEND = "net.backupcup.hexed.compat.flame.PrometheusFlameBackend";
    private static final String DYED_FLAMES_BACKEND = "net.backupcup.hexed.compat.flame.DyedFlamesFlameBackend";

    @Nullable
    private static Backend backend;
    @Nullable
    private static String backendMod;

    private HexedFlames() {}

    /** Picks the flame mod to use. Prometheus wins when both are installed. */
    public static void init() {
        boolean prometheus = Services.PLATFORM.isModLoaded(PROMETHEUS);
        boolean dyedFlames = Services.PLATFORM.isModLoaded(DYED_FLAMES);
        if (prometheus && dyedFlames) {
            Hexed.LOGGER.warn("Both Prometheus and Dyed Flames are installed; Hexed uses Prometheus for its flames. These two mods often conflict.");
        }

        if (prometheus) {
            backendMod = PROMETHEUS;
            backend = create(PROMETHEUS_BACKEND);
        } else if (dyedFlames) {
            backendMod = DYED_FLAMES;
            backend = create(DYED_FLAMES_BACKEND);
        } else {
            return;
        }
        backend.register();
        Hexed.LOGGER.info("Using {} for custom flames", backendMod);
    }

    private static Backend create(String className) {
        try {
            // The integrations live in the loader jars and are only loaded when their mod is present.
            return (Backend) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Couldn't load flame integration " + className, e);
        }
    }

    /** The flame mod in use, or {@code null} when neither is installed (which the loaders treat as an error). */
    @Nullable
    public static String activeMod() {
        return backendMod;
    }

    public static String missingMessage() {
        return "Hexed needs one flame mod: install either Prometheus (https://modrinth.com/mod/prometheus-api) "
            + "or Dyed Flames (https://modrinth.com/mod/dyed-flames). Only one is needed.";
    }

    public static void show(Entity entity, Flame flame) {
        if (backend != null && !entity.level().isClientSide()) backend.show(entity, flame);
    }

    public static void clear(Entity entity, Flame flame) {
        if (backend != null && !entity.level().isClientSide()) backend.clear(entity, flame);
    }

    /** Whether any part of the entity is inside Blazing Magma. */
    public static boolean isInBlazingMagma(Entity entity) {
        AABB box = entity.getBoundingBox().deflate(1.0E-3);
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            if (RegisterSlagBlocks.BLAZING_MAGMA_SOURCE.get().isSame(entity.level().getFluidState(pos).getType())) return true;
        }
        return false;
    }
}
