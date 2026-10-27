package net.backupcup.hexed.register;

import net.backupcup.hexed.altar.AccursedAltarBlockEntity;
import net.backupcup.hexed.registry.Reg;
import net.backupcup.hexed.registry.RegObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class RegisterBlockEntities {
    @SuppressWarnings("DataFlowIssue")
    public static final RegObject<BlockEntityType<AccursedAltarBlockEntity>> ACCURSED_ALTAR = Reg.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
        "accursed_altar_block_entity",
        () -> BlockEntityType.Builder.of(AccursedAltarBlockEntity::new, RegisterBlocks.ACCURSED_ALTAR.get()).build(null));

    private RegisterBlockEntities() {}

    public static void init() {}
}
