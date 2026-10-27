package net.backupcup.hexed.neoforge.gametest;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.gametest.HexedBrimstoneFireTest;
import net.backupcup.hexed.gametest.HexedFlameTest;
import net.backupcup.hexed.gametest.HexedSmokeTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Discovered through {@link GameTestHolder}; its namespace must be listed in {@code neoforge.enabledGameTestNamespaces}. */
@Mod("hexed_gametest")
@GameTestHolder(Hexed.MOD_ID)
@PrefixGameTestTemplate(false)
public class HexedNeoForgeGameTests {
    // NeoForge adds the holder's namespace to the template itself.
    @GameTest(template = "empty")
    public static void smoke(GameTestHelper helper) {
        HexedSmokeTest.run(helper);
    }

    @GameTest(template = "empty")
    public static void flames(GameTestHelper helper) {
        HexedFlameTest.run(helper);
    }

    @GameTest(template = "empty")
    public static void brimstoneFire(GameTestHelper helper) {
        HexedBrimstoneFireTest.run(helper);
    }
}
