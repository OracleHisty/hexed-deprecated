package net.backupcup.hexed.client;

import net.backupcup.hexed.Config;
import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.client.screen.AccursedAltarScreen;
import net.backupcup.hexed.network.*;
import net.backupcup.hexed.util.HexRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Handlers for every clientbound payload. Loaders route received payloads to {@link #handle}. */
public final class HexedClientNetwork {
    private static final DustParticleOptions BLOOD = new DustParticleOptions(new Vector3f(0.6f, 0f, 0f), 1.5f);

    private HexedClientNetwork() {}

    public static void handle(CustomPacketPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        switch (payload) {
            case SyncConfigPayload sync -> {
                Config config = Config.fromJson(sync.json());
                if (config != null) Hexed.setConfig(config);
            }
            case AltarHexesPayload hexes -> {
                if (minecraft.screen instanceof AccursedAltarScreen screen) {
                    screen.updateHexes(hexes.hexes(), hexes.current());
                }
            }
            case HexUiUpdatePayload ui -> HexedHud.update(ui);
            case BloodthirstyParticlesPayload blood -> {
                if (minecraft.level == null) return;
                Vec3 pos = blood.pos();
                for (int i = 0; i <= blood.amount(); i++) {
                    minecraft.level.addParticle(BLOOD, pos.x + jitter(), pos.y + 0.5 + jitter(), pos.z + jitter(), 0.0, 0.5, 0.0);
                }
            }
            case LingerParticlePayload linger -> {
                if (minecraft.level == null) return;
                Vec3 pos = linger.pos();
                minecraft.level.addParticle(DustParticleOptions.REDSTONE, pos.x, pos.y, pos.z, 0.0, 0.5, 0.0);
            }
            case BasherParticlesPayload basher -> basherParticles(minecraft, basher);
            default -> Hexed.LOGGER.warn("Unhandled clientbound payload {}", payload.type().id());
        }
    }

    private static void basherParticles(Minecraft minecraft, BasherParticlesPayload payload) {
        if (minecraft.level == null) return;

        for (Vec3 hit : payload.hits()) {
            for (int i = 2; i <= 10; i++) {
                minecraft.level.addParticle(BLOOD, hit.x + jitter(), hit.y + 0.5 + jitter(), hit.z + jitter(), jitter(), 0.5, jitter());
            }
        }

        Vec3 center = payload.center();
        double radius = payload.radius();
        int points = (int) (2 * Math.PI * radius * 2);
        for (int i = 0; i < points; i++) {
            double angle = i * 2 * Math.PI / points;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            minecraft.level.addParticle(BLOOD, x + jitter(), center.y + 0.5 + jitter(), z + jitter(), jitter(), 0.5, jitter());
        }
    }

    private static double jitter() {
        return HexRandom.nextDouble(-0.5, 0.5) * (HexRandom.nextBoolean() ? -1 : 1);
    }
}
