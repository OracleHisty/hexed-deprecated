package net.backupcup.hexed.register;

import net.backupcup.hexed.entity.BlazingSkullEntity;
import net.backupcup.hexed.registry.Reg;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class RegisterEntities {
    public static final RegObject<EntityType<BlazingSkullEntity>> BLAZING_SKULL = Reg.register(BuiltInRegistries.ENTITY_TYPE, "blazing_skull",
        () -> EntityType.Builder.<BlazingSkullEntity>of(BlazingSkullEntity::new, MobCategory.MONSTER)
            .sized(0.75f, 0.9375f)
            .build("blazing_skull"));

    private RegisterEntities() {}

    public static void init() {}
}
