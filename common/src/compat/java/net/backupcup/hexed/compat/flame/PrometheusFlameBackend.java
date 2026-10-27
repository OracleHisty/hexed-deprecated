package net.backupcup.hexed.compat.flame;

import it.crystalnest.prometheus.api.Fire;
import it.crystalnest.prometheus.api.FireManager;
import it.crystalnest.prometheus.api.type.FireTypeSynched;
import net.backupcup.hexed.flame.HexedFlames;
import net.backupcup.hexed.register.RegisterBlocks;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;

/**
 * Registers each flame as a Prometheus fire type, whose textures Prometheus loads from
 * {@code hexed:block/<flame>_fire_0/1}. An entity's flames follow its fire type.
 */
public class PrometheusFlameBackend implements HexedFlames.Backend {
    @Override
    public void register() {
        // No other components: Hexed adds no Prometheus torches, campfires or lanterns.
        FireManager.registerFire(FireManager.fireBuilder(HexedFlames.Flame.AFLAME.id()).build());
        // Naming Brimstone Fire as the source block makes Prometheus give anything burning in it this fire type.
        FireManager.registerFire(FireManager.fireBuilder(HexedFlames.Flame.BRIMSTONE.id())
            .setComponent(Fire.Component.SOURCE_BLOCK, RegisterBlocks.BRIMSTONE_FIRE.id())
            .build());
    }

    @Override
    public void show(Entity entity, HexedFlames.Flame flame) {
        // Written directly because FireTypeChanger#setFireType skips fire-immune entities, which Aflame still burns.
        EntityDataAccessor<String> accessor = ((FireTypeSynched) entity).fireTypeAccessor();
        String type = flame.id().toString();
        if (!type.equals(entity.getEntityData().get(accessor))) {
            entity.getEntityData().set(accessor, type);
        }
    }

    @Override
    public void clear(Entity entity, HexedFlames.Flame flame) {
        EntityDataAccessor<String> accessor = ((FireTypeSynched) entity).fireTypeAccessor();
        if (flame.id().toString().equals(entity.getEntityData().get(accessor))) {
            entity.getEntityData().set(accessor, FireManager.DEFAULT_FIRE_TYPE.toString());
        }
    }
}
