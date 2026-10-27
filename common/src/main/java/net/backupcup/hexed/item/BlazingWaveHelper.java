package net.backupcup.hexed.item;

import net.backupcup.hexed.network.BasherParticlesPayload;
import net.backupcup.hexed.network.HexedNetwork;
import net.backupcup.hexed.register.RegisterDamageTypes;
import net.backupcup.hexed.register.RegisterSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class BlazingWaveHelper {
    private BlazingWaveHelper() {}

    /** Damages everything around {@code center}, more the closer it is, and shows the wave to nearby players. */
    public static void createExplosion(LivingEntity center, double radius) {
        AABB area = new AABB(center.blockPosition()).inflate(radius);
        List<Vec3> hitPositions = new ArrayList<>();

        for (LivingEntity entity : center.level().getEntitiesOfClass(LivingEntity.class, area)) {
            float distance = entity.distanceTo(center);
            if (distance > radius) continue;
            entity.hurt(RegisterDamageTypes.of(entity.level(), RegisterDamageTypes.BLAZING_WAVE), (float) radius - distance);
            hitPositions.add(entity.position());
        }

        HexedNetwork.sendToTracking(center, new BasherParticlesPayload(radius, center.position(), hitPositions));

        center.level().playSound(null, center.blockPosition(), RegisterSounds.ACCURSED_ALTAR_TAINT.get(), SoundSource.BLOCKS, 0.25f, 2f);
    }
}
