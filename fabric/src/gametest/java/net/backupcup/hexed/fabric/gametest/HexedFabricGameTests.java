package net.backupcup.hexed.fabric.gametest;

import net.backupcup.hexed.gametest.HexedBrimstoneFireTest;
import net.backupcup.hexed.gametest.HexedFlameTest;
import net.backupcup.hexed.gametest.HexedSmokeTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class HexedFabricGameTests implements FabricGameTest {
    @GameTest(template = HexedSmokeTest.TEMPLATE)
    public void smoke(GameTestHelper helper) {
        HexedSmokeTest.run(helper);
    }

    @GameTest(template = HexedSmokeTest.TEMPLATE)
    public void flames(GameTestHelper helper) {
        HexedFlameTest.run(helper);
    }

    @GameTest(template = HexedSmokeTest.TEMPLATE)
    public void brimstoneFire(GameTestHelper helper) {
        HexedBrimstoneFireTest.run(helper);
    }
}
