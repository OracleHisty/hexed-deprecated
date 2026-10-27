package net.backupcup.hexed.gametest;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.hex.Hexes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.Map;

/**
 * Loader-independent smoke test, run by each loader's GameTest server ({@code runGametest} / {@code runGameTestServer}).
 * Not part of the mod jars.
 */
public final class HexedSmokeTest {
    public static final String TEMPLATE = "hexed:empty";

    private HexedSmokeTest() {}

    public static void run(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();

        // Applies every mixin to its target now, so a broken injection point fails here instead of in game.
        MixinEnvironment.getCurrentEnvironment().audit();

        for (ResourceKey<Enchantment> hex : Hexes.ALL) {
            helper.assertTrue(HexHelper.lookup(level.registryAccess(), hex).isPresent(), "Missing hex enchantment " + hex.location());
        }
        helper.assertTrue(Hexes.ALL.size() == 30, "Expected 30 hexes, found " + Hexes.ALL.size());

        Map<Item, Integer> expected = Map.of(
            Items.DIAMOND_SWORD, 5,
            Items.NETHERITE_CHESTPLATE, 4,
            Items.IRON_BOOTS, 4,
            Items.BOW, 4,
            Items.CROSSBOW, 3,
            Items.TRIDENT, 4,
            Items.DIAMOND_PICKAXE, 4,
            Items.STICK, 0);
        expected.forEach((item, count) -> {
            int found = HexHelper.availableHexes(level, new ItemStack(item)).size();
            helper.assertTrue(found == count, item + " should offer " + count + " hexes, offers " + found);
        });

        // Hexes alone don't count as enchantments.
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(holder(level, Hexes.AFLAME), 1);
        helper.assertTrue(HexHelper.has(sword, Hexes.AFLAME), "Aflame wasn't applied");
        helper.assertFalse(sword.isEnchanted(), "A hex-only item shouldn't count as enchanted");
        helper.assertTrue(sword.isEnchantable(), "A hex-only item should still be enchantable");
        helper.assertTrue(HexHelper.availableHexes(level, sword).size() == 4, "Applied hexes shouldn't be offered again");

        // Displaced swaps armor and toughness.
        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.enchant(holder(level, Hexes.DISPLACED), 1);
        double[] armor = attributeTotals(chestplate);
        helper.assertTrue(armor[0] == 2.0 && armor[1] == 8.0, "Displaced should swap 8 armor / 2 toughness, got " + armor[0] + " / " + armor[1]);

        // Bloodthirsty's max health comes from its enchantment JSON.
        ItemStack robes = new ItemStack(Items.IRON_CHESTPLATE);
        robes.enchant(holder(level, Hexes.BLOODTHIRSTY), 1);
        double[] health = {0};
        robes.forEachModifier(EquipmentSlot.CHEST, (attribute, modifier) -> {
            if (attribute.is(Attributes.MAX_HEALTH)) health[0] += modifier.amount();
        });
        helper.assertTrue(health[0] == 8.0, "Bloodthirsty should add 8 max health, adds " + health[0]);

        // The converted data pack loaded.
        helper.assertTrue(server.getRecipeManager().byKey(Hexed.id("accursed_altar")).isPresent(), "Altar recipe missing");
        helper.assertTrue(server.getRecipeManager().byKey(Hexed.id("tall_red_candle")).isPresent(), "Candle recipe missing");
        helper.assertTrue(server.getAdvancements().get(Hexed.id("ultrakill")) != null, "Advancement missing");
        helper.assertTrue(server.getAdvancements().get(Hexed.id("fuller_auto")) != null, "Advancement missing");
        LootTable bricks = server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, Hexed.id("blocks/brimstone_bricks")));
        helper.assertTrue(bricks != LootTable.EMPTY, "Block loot table missing");
        helper.assertTrue(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).containsKey(ResourceLocation.fromNamespaceAndPath("hexed", "abyssal_crush")),
            "Damage type missing");

        helper.succeed();
    }

    private static Holder<Enchantment> holder(ServerLevel level, ResourceKey<Enchantment> key) {
        return HexHelper.lookup(level.registryAccess(), key).orElseThrow();
    }

    private static double[] attributeTotals(ItemStack stack) {
        double[] totals = {0, 0};
        stack.forEachModifier(EquipmentSlot.CHEST, (attribute, modifier) -> {
            if (attribute.is(Attributes.ARMOR)) totals[0] += modifier.amount();
            else if (attribute.is(Attributes.ARMOR_TOUGHNESS)) totals[1] += modifier.amount();
        });
        return totals;
    }
}
