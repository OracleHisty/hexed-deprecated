package net.backupcup.hexed.gametest;

import fuzs.dyedflames.init.ModRegistry;
import it.crystalnest.prometheus.api.type.FireTyped;
import net.backupcup.hexed.effect.AflameMobEffect;
import net.backupcup.hexed.flame.HexedFlames;
import net.backupcup.hexed.register.RegisterBlocks;
import net.backupcup.hexed.register.RegisterItems;
import net.backupcup.hexed.register.RegisterStatusEffects;
import net.backupcup.hexed.util.AflameBurning;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;

/** Aflame and the flame mod integration, against whichever flame mod the dev run loaded. */
public final class HexedFlameTest {
    private HexedFlameTest() {}

    public static void run(GameTestHelper helper) {
        String flameMod = HexedFlames.activeMod();
        helper.assertTrue(flameMod != null, "No flame mod is active");

        // The Ender Dragon refuses every effect; Aflame still lands.
        EnderDragon dragon = EntityType.ENDER_DRAGON.create(helper.getLevel());
        helper.assertTrue(dragon != null, "Couldn't create a dragon");
        AflameMobEffect.apply(dragon, 100, 0, null);
        helper.assertTrue(dragon.hasEffect(RegisterStatusEffects.AFLAME.holder()), "Aflame should ignore effect immunity");

        // The full Calamitous set keeps it off.
        Zombie robed = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, BlockPos.ZERO.above());
        robed.setItemSlot(EquipmentSlot.HEAD, new ItemStack(RegisterItems.CALAMITOUS_HELMET.get()));
        robed.setItemSlot(EquipmentSlot.CHEST, new ItemStack(RegisterItems.CALAMITOUS_CHESTPLATE.get()));
        robed.setItemSlot(EquipmentSlot.LEGS, new ItemStack(RegisterItems.CALAMITOUS_LEGGINGS.get()));
        robed.setItemSlot(EquipmentSlot.FEET, new ItemStack(RegisterItems.CALAMITOUS_BOOTS.get()));
        AflameMobEffect.apply(robed, 100, 0, null);
        helper.assertFalse(robed.hasEffect(RegisterStatusEffects.AFLAME.holder()), "The Calamitous set should block Aflame");

        // Husks don't burn in daylight, which would otherwise set real fire.
        Husk zombie = helper.spawnWithNoFreeWill(EntityType.HUSK, BlockPos.ZERO.above());
        AflameMobEffect.apply(zombie, 100, 0, null);
        helper.assertTrue(zombie.hasEffect(RegisterStatusEffects.AFLAME.holder()), "Aflame wasn't applied");

        helper.runAfterDelay(2, () -> {
            helper.assertTrue(((AflameBurning) zombie).hexed$isAflameBurning(), "The Aflame flag didn't sync");
            helper.assertTrue(zombie.displayFireAnimation(), "Aflame creatures should be drawn burning");
            helper.assertFalse(zombie.isOnFire(), "Aflame shouldn't set real fire");

            if (HexedFlames.PROMETHEUS.equals(flameMod)) {
                helper.assertTrue(HexedFlames.Flame.AFLAME.id().equals(((FireTyped) zombie).getFireType()),
                    "Prometheus fire type should be hexed:aflame, is " + ((FireTyped) zombie).getFireType());
            } else {
                helper.assertTrue(ModRegistry.LAST_FIRE_SOURCE_ATTACHMENT_TYPE.get(zombie) == RegisterBlocks.AFLAME_FLAME.get(),
                    "Dyed Flames fire source should be hexed:aflame_flame");
            }

            zombie.removeEffect(RegisterStatusEffects.AFLAME.holder());
            helper.runAfterDelay(2, () -> {
                helper.assertFalse(zombie.displayFireAnimation(), "Flames should stop with the effect");
                if (HexedFlames.PROMETHEUS.equals(flameMod)) {
                    helper.assertFalse(HexedFlames.Flame.AFLAME.id().equals(((FireTyped) zombie).getFireType()), "Prometheus fire type wasn't cleared");
                } else {
                    helper.assertTrue(ModRegistry.LAST_FIRE_SOURCE_ATTACHMENT_TYPE.get(zombie) == null, "Dyed Flames fire source wasn't cleared");
                }
                helper.succeed();
            });
        });
    }
}
