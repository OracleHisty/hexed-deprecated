package net.backupcup.hexed.loot;

import net.backupcup.hexed.register.RegisterItems;
import net.backupcup.hexed.register.RegisterPlushies;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.Consumer;

/** Extra pools added to vanilla loot tables. Each loader calls this from its loot table event. */
public final class HexedLoot {
    private HexedLoot() {}

    public static void modify(ResourceKey<LootTable> id, Consumer<LootPool.Builder> addPool) {
        if (id.location().getPath().startsWith("chests")) {
            addPool.accept(RegisterPlushies.chestPool());
        }

        int fabricWeight;
        if (id.equals(BuiltInLootTables.BASTION_TREASURE)) fabricWeight = 40;
        else if (id.equals(BuiltInLootTables.BASTION_BRIDGE)) fabricWeight = 18;
        else if (id.equals(BuiltInLootTables.BASTION_OTHER)) fabricWeight = 12;
        else return;

        int crystalWeight = id.equals(BuiltInLootTables.BASTION_TREASURE) ? 100 : 0;
        LootPool.Builder pool = LootPool.lootPool()
            .setRolls(UniformGenerator.between(0.25f, 1f))
            .add(LootItem.lootTableItem(RegisterItems.CALAMITOUS_FABRIC.get()).setWeight(fabricWeight));
        if (crystalWeight > 0) {
            pool.add(LootItem.lootTableItem(RegisterItems.BRIMSTONE_CRYSTAL.get()).setWeight(crystalWeight));
        }
        addPool.accept(pool);
    }
}
